package org.dice_research.ldcbench.jsonld.gen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.htmlembd.SimpleHEComponent;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;
import org.dice_research.ldcbench.utils.tar.TarFileGenerator;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLDDataGenerator extends DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLDDataGenerator.class);

    //TODO wtf is this?
    // protected static final String[] RESOURCE_URI_TEMPLATES = new String[] { "http://domain0.org/%s-%s/%s-%s",
    // "http://domain1.org/%s-%s/%s-%s", "http://domain2.org/%s-%s/%s-%s", "http://domain3.org/%s-%s/%s-%s",
    // "http://domain4.org/%s-%s/%s-%s", "http://domain5.org/%s-%s/%s-%s", "http://domain6.org/%s-%s/%s-%s",
    // "http://domain7.org/%s-%s/%s-%s", "http://domain8.org/%s-%s/%s-%s", "http://domain9.org/%s-%s/%s-%s"};
    // protected static final String[] ACCESS_URI_TEMPLATES = new String[] { "http://domain0.org/%s-%s/%s-%s",
    // "http://domain1.org/%s-%s/%s-%s", "http://domain2.org/%s-%s/%s-%s", "http://domain3.org/%s-%s/%s-%s",
    // "http://domain4.org/%s-%s/%s-%s", "http://domain5.org/%s-%s/%s-%s", "http://domain6.org/%s-%s/%s-%s",
    // "http://domain7.org/%s-%s/%s-%s", "http://domain8.org/%s-%s/%s-%s", "http://domain9.org/%s-%s/%s-%s"};


    protected static final String HTML_PREFIX = "<!DOCTYPE>\r\n<html>\r\n<head>\r\n   <title>Test</title>\r\n</head>\r\n<body>";
    protected static final String HTML_SUFFIX = "</body>\n</html>";

    protected static final String JSON_LD_PREFIX = "<script type=\"application/ld+json\">";
    protected static final String JSON_LD_SUFFIX = "</script>\r\n";

    protected void generateHtmlFiles(Graph[] graphs, File htmlOut) {


        String htmlFileContent = new String();
        int domainId = 1;

        htmlFileContent += HTML_PREFIX;

        TripleIterator iterator;
        // convert Graph to jena.graph s.t. we can use the RDFDataMgr
        Model model = ModelFactory.createDefaultModel();
        org.apache.jena.graph.Graph modelGraph = model.getGraph();
        int datasetId = 0;
        for(Graph graph: graphs) {
            for (int i = 0; i < graph.getNumberOfNodes(); ++i) {
                iterator = new TripleIterator(graphs, domainId, resourceUriTemplates, accessUriTemplates, datasetId, i);
                while(iterator.hasNext()) {
                    modelGraph.add(iterator.next());
                }
            }
            datasetId++;
        }

        OutputStream outputJLD = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b );
            }

            public String toString() {
                return this.string.toString();
            }
        };
        RDFDataMgr.write(outputJLD, model, Lang.JSONLD);

        htmlFileContent += JSON_LD_PREFIX;
        htmlFileContent += outputJLD.toString();
        htmlFileContent += JSON_LD_SUFFIX;

        htmlFileContent += HTML_SUFFIX;
        System.out.println(htmlFileContent);

        // //create TTL file

        // OutputStream outputTTL = new OutputStream() {
        //     private StringBuilder string = new StringBuilder();

        //     @Override
        //     public void write(int b) throws IOException {
        //         this.string.append((char) b );
        //     }

        //     public String toString() {
        //         return this.string.toString();
        //     }
        // };
        // RDFDataMgr.write(outputTTL, model, Lang.TTL);
        // System.out.println("TTL: " + outputTTL.toString());
    }

    @Override
    protected void sendFinalGraph(Graph graph) throws Exception {
        // iterate over the available test sets
        // LOGGER.info("Reading manifest files of different test cases...");
        // Map<String, String> tests = loadTestFiles();
        // LOGGER.info("Found {} test files. Processing them...", tests.size());
        // SortedMap<String, File> htmlFiles = new TreeMap<>();
        // SortedMap<String, File> ttlFiles = new TreeMap<>();
        // convertTestUrisToFiles(tests.keySet(), htmlFiles);
        // convertTestUrisToFiles(tests.values(), ttlFiles);

        // // Generate URL of resources on the node
        // String nodeDomain = accessUriTemplates[getNodeId()].replace(ACCESS_URI_TEMPLATE_PATTERN,
        //         "") + "/";
        // // Load the tests and replace URIs
        // replaceUrisInFiles(htmlFiles, RDFA_TEST_DOMAIN, nodeDomain);
        // replaceUrisInFiles(ttlFiles, RDFA_TEST_DOMAIN, nodeDomain);
        // htmlFiles = replaceUrisInMapping(htmlFiles, RDFA_TEST_DOMAIN, nodeDomain);
        // ttlFiles = replaceUrisInMapping(ttlFiles, RDFA_TEST_DOMAIN, nodeDomain);

        // // Generate HTML and TTL file based on the graph and the list of test files
        // generateEntranceFile(graph, htmlFiles, ENTRANCE_HTML_FILE, ENTRANCE_TTL_FILE);
        // // Add entrance file to the list of HTML and ttl files
        // htmlFiles.put(ENTRANCE_HTML_FILE_NAME, ENTRANCE_HTML_FILE);
        // ttlFiles.put(ENTRANCE_TTL_FILE_NAME, ENTRANCE_TTL_FILE);

        // String filePrefix = String.format("graph-%0" + (int) Math.ceil(Math.log10(getNumberOfGenerators() + 1)) + "d",
        //         getNodeId());

        File htmlOut = new File(SimpleHEComponent.SINGLE_FILE_NAME);

        Graph[] g = {graph};
        generateHtmlFiles(g, htmlOut);


        SortedMap<String, File> htmlFiles = new TreeMap<>();

        htmlFiles.put(SimpleHEComponent.SINGLE_FILE_NAME, htmlOut);
        // SortedMap<String, File> ttlFiles = new TreeMap<>();


        // Generate HTML tar file
        generateAndSendTarFile(SimpleHEComponent.SINGLE_FILE_NAME + ApiConstants.FILE_ENDING_HTML_TAR_GZ, htmlFiles, outgoingDataQueuefactory, dataQueueName);

        // // Generate TTL tar file
        // generateAndSendTarFile(filePrefix + ApiConstants.FILE_ENDING_TTL_TAR_GZ, ttlFiles, outgoingDataQueuefactory,
        //         evalDataQueueName);
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
