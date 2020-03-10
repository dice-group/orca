package org.dice_research.ldcbench.rdfa.gen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.SimpleTripleCreator;
import org.dice_research.ldcbench.utils.tar.TarFileGenerator;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFaDataGenerator extends DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDFaDataGenerator.class);

    public static final String ENTRANCE_FILE_NAME = "entrance.html";
    public static final String ACCESS_URI_TEMPLATE_PATTERN = "/%s-%s/%s-%s";
    
    protected static final File ENTRANCE_FILE = new File(ENTRANCE_FILE_NAME);
    protected static final String RDFA_TEST_DOMAIN = "http://rdfa.info/test-suite/test-cases/";
    protected static final String TEST_ROOT_DIRECTORY = "rdfa.github.io/test-suite/test-cases/";
    protected static final String TEST_CASES[] = new String[] { "rdfa1.0/html4", "rdfa1.0/xhtml1", "rdfa1.1/html4",
            "rdfa1.1/html5", "rdfa1.1/xhtml1", "rdfa1.1/xhtml5" };

    @Override
    protected void sendFinalGraph(Graph graph) throws Exception {
        // iterate over the available test sets
        LOGGER.info("Reading manifest files of different test cases...");
        Map<String, String> tests = loadTestFiles();
        LOGGER.info("Found {} test files. Processing them...", tests.size());
        SortedMap<String, File> htmlFiles = new TreeMap<>();
        SortedMap<String, File> ttlFiles = new TreeMap<>();
        convertTestUrisToFiles(tests.keySet(), htmlFiles);
        convertTestUrisToFiles(tests.values(), ttlFiles);

        // Generate URL of resources on the node
        String nodeDomain = accessUriTemplates[getNodeId()].replace(ACCESS_URI_TEMPLATE_PATTERN,
                "");
        // Load the tests and replace URIs
        replaceUrisInFiles(htmlFiles.values(), RDFA_TEST_DOMAIN, nodeDomain);
        replaceUrisInFiles(ttlFiles.values(), RDFA_TEST_DOMAIN, nodeDomain);
        htmlFiles = replaceUrisInMapping(htmlFiles, RDFA_TEST_DOMAIN, nodeDomain);

        // Generate HTML file based on the graph and the list of test files
        generateEntranceFile(graph, htmlFiles, ENTRANCE_FILE);
        // Add entrance file to the list of HTML files
        htmlFiles.put(String.format(accessUriTemplates[getNodeId()], "dataset", "0", "resource", "0"), ENTRANCE_FILE);

        String filePrefix = String.format("graph-%0" + (int) Math.ceil(Math.log10(getNumberOfGenerators() + 1)) + "d",
                getNodeId());

        // Generate HTML tar file
        generateAndSendTarFile(filePrefix + ApiConstants.FILE_ENDING_HTML_TAR_GZ, htmlFiles, outgoingDataQueuefactory, dataQueueName);

        // Generate TTL tar file
        generateAndSendTarFile(filePrefix + ApiConstants.FILE_ENDING_TTL_TAR_GZ, ttlFiles, outgoingDataQueuefactory,
                evalDataQueueName);
    }

    protected void generateEntranceFile(Graph graph, Map<String, File> htmlFiles, File entranceFile)
            throws IOException {
        SimpleTripleCreator creator = new SimpleTripleCreator(getNodeId(), resourceUriTemplates, accessUriTemplates);

        Set<String> outgoingLinks = generateOutgoingLinks(graph, creator);
        outgoingLinks.addAll(htmlFiles.keySet());

        String entranceUri = generateEntranceNodeUri(graph, creator);

        RDFaEntranceFileGenerator generator = new RDFaEntranceFileGenerator();
        generator.generate(entranceFile, entranceUri, outgoingLinks);
    }

    protected String generateEntranceNodeUri(Graph graph, SimpleTripleCreator creator) {
        int entranceNode = graph.getEntranceNodes()[0];
        return creator
                .createNode(entranceNode, graph.getExternalNodeId(entranceNode), graph.getGraphId(entranceNode), false)
                .getURI();
    }

    protected Set<String> generateOutgoingLinks(final Graph graph, final SimpleTripleCreator creator) {
        Set<String> outgoingLinks = new HashSet<>();
        int entranceNodes[] = graph.getEntranceNodes();
        for (int i = 0; i < entranceNodes.length; ++i) {
            outgoingLinks.addAll(Arrays.stream(graph.outgoingEdgeTargets(entranceNodes[i]))
                    .mapToObj(id -> creator.createNode(id, graph.getExternalNodeId(id), graph.getGraphId(id), false))
                    .map(n -> n.getURI()).collect(Collectors.toSet()));
        }
        return outgoingLinks;
    }

    protected static void convertTestUrisToFiles(Collection<String> testUris, Map<String, File> files) {
        for (String uri : testUris) {
            uri = uri.substring(RDFA_TEST_DOMAIN.length());
            files.put(uri, new File(TEST_ROOT_DIRECTORY + uri));
        }
    }

    protected SortedMap<String, File> replaceUrisInMapping(Map<String, File> uriFileMapping, String regex,
            String replacement) {
        final SortedMap<String, File> newMap = new TreeMap<>();
        uriFileMapping.entrySet().stream()
                .forEach(e -> newMap.put(e.getKey().replaceAll(regex, replacement), e.getValue()));
        return newMap;
    }

    protected void replaceUrisInFiles(Collection<File> values, String regex, String replacement) {
        String fileContent;
        for (File f : values) {
            try {
                fileContent = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                fileContent.replaceAll(regex, replacement);
                FileUtils.write(f, fileContent, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Couldn't update file content of " + f.getAbsolutePath() + ". It will be ignored.", e);
            }
        }
    }

    protected Map<String, String> loadTestFiles() {
        ManifestProcessor processor = new ManifestProcessor();
        Map<String, String> testFiles = new HashMap<>();
        for (int i = 0; i < TEST_CASES.length; ++i) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(TEST_ROOT_DIRECTORY);
            pathBuilder.append(TEST_CASES[i]);
            pathBuilder.append("/manifest.ttl");
            testFiles.putAll(processor.loadTests(pathBuilder.toString()));
        }
        return testFiles;
    }

    protected void generateAndSendTarFile(String file, SortedMap<String, File> files,
            RabbitQueueFactory outgoingDataQueuefactory, String dataQueueName) throws IOException {
        File tarFile = new File(file);
        // Create file
        TarFileGenerator generator = new TarFileGenerator();
        generator.generateTarFile(tarFile, files, true);

        // Send file
        sendTarFile(tarFile, outgoingDataQueuefactory, dataQueueName);
    }

    protected static void sendTarFile(File file, RabbitQueueFactory outgoingDataQueuefactory, String dataQueueName)
            throws IOException {
        // Create file
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
                SimpleFileSender dataSender = SimpleFileSender.create(outgoingDataQueuefactory, dataQueueName);) {
            dataSender.streamData(is, file.getName());
        }
    }

}
