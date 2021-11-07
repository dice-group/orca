package org.dice_research.ldcbench.microformat.gen;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

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

public class MicroformatGenerator extends DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroformatGenerator.class);

    public static final String ENTRANCE_HTML_FILE_NAME = "entrance.html";
    public static final String ENTRANCE_TTL_FILE_NAME = "entrance.ttl";
    public static final String ACCESS_URI_TEMPLATE_PATTERN = "/%s-%s/%s-%s";

    protected static final File ENTRANCE_HTML_FILE = new File(ENTRANCE_HTML_FILE_NAME);
    protected static final File ENTRANCE_TTL_FILE = new File(ENTRANCE_TTL_FILE_NAME);

    // "../ldcbench.microformat-gen/" makes it work in ldcbench.controller tests
    protected static final String TEST_ROOT_DIRECTORY = "../ldcbench.microformat-gen/microformats.tests/tests/";

    protected static final String MICROFORMAT_TEST_DOMAIN = "http://microformats.org/wiki/test-suite/tests/"; //TODO?

    protected static final String TEST_CASES[] = new String[] { "microformats-mixed", "microformats-v1", "microformats-v2" };

    @Override
    protected void sendFinalGraph(Graph graph) throws Exception {
        // there is no manifest-file, so we need to find the test-cases
        LOGGER.info("Crawling the test cases...");
        Map<String, String> tests = loadTestFiles();
        LOGGER.info("Found {} test files. Processing them...", tests.size());


        SortedMap<String, File> htmlFiles = new TreeMap<>();
        SortedMap<String, File> ttlFiles = new TreeMap<>();
        convertTestFilesToUris(tests.keySet(), htmlFiles);
        convertTestFilesToUris(tests.values(), ttlFiles);

        // Generate URL of resources on the node
        String nodeDomain = accessUriTemplates[getNodeId()].replace(ACCESS_URI_TEMPLATE_PATTERN,
                "") + "/";
        // Load the tests and replace URIs
        replaceUrisInFiles(htmlFiles, MICROFORMAT_TEST_DOMAIN, nodeDomain);
        replaceUrisInFiles(ttlFiles, MICROFORMAT_TEST_DOMAIN, nodeDomain);
        htmlFiles = replaceUrisInMapping(htmlFiles, MICROFORMAT_TEST_DOMAIN, nodeDomain);
        ttlFiles = replaceUrisInMapping(ttlFiles, MICROFORMAT_TEST_DOMAIN, nodeDomain);

        // Generate HTML and TTL file based on the graph and the list of test files
//TODO!
        // generateEntranceFile(graph, htmlFiles, ENTRANCE_HTML_FILE, ENTRANCE_TTL_FILE);
        // Add entrance file to the list of HTML and ttl files
        htmlFiles.put(ENTRANCE_HTML_FILE_NAME, ENTRANCE_HTML_FILE);
        ttlFiles.put(ENTRANCE_TTL_FILE_NAME, ENTRANCE_TTL_FILE);

        String filePrefix = String.format("graph-%0" + (int) Math.ceil(Math.log10(getNumberOfGenerators() + 1)) + "d",
                getNodeId());

        // Generate HTML tar file
        generateAndSendTarFile(filePrefix + ApiConstants.FILE_ENDING_HTML_TAR_GZ, htmlFiles, outgoingDataQueuefactory, dataQueueName);

        // Generate TTL tar file
        generateAndSendTarFile(filePrefix + ApiConstants.FILE_ENDING_TTL_TAR_GZ, ttlFiles, outgoingDataQueuefactory,
                evalDataQueueName);
    }

    protected void generateEntranceFile(Graph graph, Map<String, File> htmlFiles, File entranceFile, File entranceTTLFile)
            throws IOException {
        // SimpleTripleCreator creator = new SimpleTripleCreator(getNodeId(), resourceUriTemplates, accessUriTemplates);

        // Set<String> outgoingLinks = generateOutgoingLinks(graph, creator);
        // outgoingLinks.addAll(htmlFiles.keySet());

        // String entranceUri = generateEntranceNodeUri(graph, creator);

        // MicrodataEntranceFileGenerator generator = new MicrodataEntranceFileGenerator();
        // generator.generate(entranceFile, entranceTTLFile, entranceUri, outgoingLinks);
    }

    protected String generateEntranceNodeUri(Graph graph, SimpleTripleCreator creator) {
        // int entranceNode = graph.getEntranceNodes()[0];
        // return creator
        //         .createNode(entranceNode, graph.getExternalNodeId(entranceNode), graph.getGraphId(entranceNode), false)
        //         .getURI();
        return null;
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

    protected static void convertTestFilesToUris(Collection<String> testFiles, Map<String, File> files) {
        for (String file : testFiles) {
            String[] pathElements = file.split("/");
            int elementsCount = pathElements.length;
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(MICROFORMAT_TEST_DOMAIN);
            uriBuilder.append(pathElements[elementsCount-3] + "/");
            uriBuilder.append(pathElements[elementsCount-2] + "/");
            uriBuilder.append(pathElements[elementsCount-1]);
            files.put(uriBuilder.toString(), new File(file));
        }
    }

    protected static SortedMap<String, File> replaceUrisInMapping(Map<String, File> uriFileMapping, String regex,
            String replacement) {
        final SortedMap<String, File> newMap = new TreeMap<>();
        uriFileMapping.entrySet().stream()
                .forEach(e -> newMap.put(e.getKey().replaceAll(regex, replacement), e.getValue()));
        return newMap;
    }

    protected static void replaceUrisInFiles(Map<String, File> files, String regex, String replacement) {
        String fileContent;
        File origFile = null;
        File newFile;
        for (Entry<String, File> entry : files.entrySet()) {
            try {
                origFile = entry.getValue();
                newFile = File.createTempFile("new", origFile.getName());
                newFile.deleteOnExit();
                fileContent = FileUtils.readFileToString(origFile, StandardCharsets.UTF_8);
                fileContent = fileContent.replaceAll(regex, replacement);
                FileUtils.write(newFile, fileContent, StandardCharsets.UTF_8);
                entry.setValue(newFile);
            } catch (IOException e) {
                LOGGER.error("Couldn't update file content of " + origFile.getAbsolutePath() + ". It will be ignored.", e);
            }
        }
    }

    /**
     * returns a Map (<PathToHtml> , <PathToTtl>) containing all tests
     */
    protected static Map<String, String> loadTestFiles() {
        Map<String, String> testFiles = new HashMap<>();

        for (int i = 0; i < TEST_CASES.length; ++i) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(TEST_ROOT_DIRECTORY);
            pathBuilder.append(TEST_CASES[i]);

            try {
                List<Path> allTests = Files.walk(Paths.get(pathBuilder.toString()))
                        .filter(s -> s.toString().endsWith(".json"))
                        .map(foo -> foo = Paths.get(foo.toString().substring(0, foo.toString().lastIndexOf('.')))) // removing the extension. TODO: clean up this line
                        .collect(Collectors.toList());
                //TODO generate ttl from json using jena

                for (Path p : allTests) {
                    testFiles.put(
                            p.toString() + ".html",
                            p.toString() + ".ttl"
                    );
                }
            } catch (IOException e) {
                LOGGER.error("Microformat-gen: Failed to load test files", e);
            }
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
