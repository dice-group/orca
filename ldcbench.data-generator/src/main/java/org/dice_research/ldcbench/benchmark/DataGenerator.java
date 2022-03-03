package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.LUBMbasedRDFGenerator;
import org.dice_research.ldcbench.generate.RandomCloudGraph;
import org.dice_research.ldcbench.generate.RandomRDF;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GraphMetadata;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.dice_research.ldcbench.graph.serialization.DumbSerializer;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.hobbit.utils.EnvVariables;
import org.hobbit.utils.rdf.RdfHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * Data generator of ORCA. It fulfills two main roles: either, it generates the
 * node graph, i.e., the graph comprising the single nodes (= servers) of the
 * synthetic cloud, or it generates the RDF graph for a single node.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class DataGenerator extends AbstractDataGenerator {
    public static final String ENV_TYPE_KEY = "LDCBENCH_DATAGENERATOR_TYPE";
    public static final String ENV_NUMBER_OF_NODES_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_NODES";
    public static final String ENV_NUMBER_OF_GRAPHS_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_GRAPHS";
    public static final String ENV_AVERAGE_DEGREE_KEY = "LDCBENCH_DATAGENERATOR_AVERAGE_DEGREE";
    public static final String ENV_BLANK_NODES_RATIO ="LDCBENCH_DATAGENERATOR_BLANK_NODES_RATIO";
    public static final String ENV_LITERALS_RATIO ="LDCBENCH_DATAGENERATOR_LITERALS_RATIO";
    public static final String ENV_NUMBER_OF_NODES_LINKS ="LDCBENCH_DATAGENERATOR_NUMBER_OF_NODES_LINKS";
    public static final String ENV_NUMBER_OF_EDGES_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_EDGES";
    public static final String ENV_DATA_QUEUE_KEY = "LDCBENCH_DATA_QUEUE";
    public static final String ENV_DATAGENERATOR_EXCHANGE_KEY = "LDCBENCH_DATAGENERATOR_EXCHANGE";
    public static final String ENV_NODETYPES_KEY = "LDCBENCH_DATAGENERATOR_NODETYPES";
    public static final String ENV_ISHUB_KEY = "LDCBENCH_DATAGENERATOR_ISHUB";
    public static final String ENV_TYPECONNECTIVITY_KEY = "LDCBENCH_DATAGENERATOR_TYPECONNECTIVITY";
    public static final String ENV_ACCESS_URI_TEMPLATES_KEY = "ACCESS_URI_TEMPLATES";
    public static final String ENV_RESOURCE_URI_TEMPLATES_KEY = "RESOURCE_URI_TEMPLATES";
    public static final String ENV_GRAPH_GENERATOR = "LDCBENCH_DATAGENERATOR_GRAPH_GENERATOR";

    /**
     * Types of data generator instances.
     *
     * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
     */
    public static enum Types {
        NODE_GRAPH_GENERATOR, RDF_GRAPH_GENERATOR
    };

    /**
     * Semaphore used to synchronize the data generators (i.e., the data generators
     * waits until all other generators are ready as well).
     */
    private Semaphore dataGeneratorsReady = new Semaphore(0);
    /**
     * Mutex used to check whether the node graph has been received.
     */
    private Semaphore nodeGraphReceivedMutex = new Semaphore(0);
    /**
     * Mutex used to check whether the node graph has been processed.
     */
    private Semaphore nodeGraphProcessedMutex = new Semaphore(0);
    /**
     * A map that stores the metadata of the single nodes in the node graph.
     */
    private Map<Integer, GraphMetadata> rdfMetadata;
    /**
     * Semaphore used to ensure that all metadata of the target nodes of this graph
     * have been received.
     */
    private Semaphore targetMetadataReceivedSemaphore = new Semaphore(0);
    /**
     * Serializer class used to serialize graphs for transmission. TODO this should
     * be received via dependency injection.
     */
    protected static final Class<DumbSerializer> SERIALIZER_CLASS = DumbSerializer.class;
    /**
     * Logger used for logging. It is created at runtime to add information about
     * the data generator.
     */
    protected Logger LOGGER;
    /**
     * The ID of this generator within the node graph.
     */
    private int generatorId = -1;
    /**
     * A generator of seed values to initialize random number generators.
     */
    private SeedGenerator seedGenerator;
    /**
     * The type of this data generator.
     */
    private Types type;
    /**
     * The number of nodes that should be generated.
     */
    private int numberOfNodes;
    /**
     * The average degree of the nodes.
     */
    private double avgDegree;
    /**
     * The number of edges that should be generated.
     */
    private int numberOfEdges;
    /**
     * The ratio of blank Nodes in the graph
     */
    private double blankNodesRatio;
    /**
     * The ratio of literals in the graph
     */
    private double literalsRatio;
    /**
     * The channel that is used for communication.
     */
    private Channel dataGeneratorsChannel;
    /**
     * The name of the exchange that is used by the data generators to communicate.
     */
    private String dataGeneratorsExchange;
    /**
     * The queue name that is used to send the generated data to the target node.
     */
    protected String dataQueueName;
    /**
     * The queue name that is used to send the generated data to the evaluation
     * module.
     */
    protected String evalDataQueueName;

    /**
     * The generator that is used to create the graph.
     */
    private GraphGenerator generator;
    /**
     * The node graph comprising the single nodes of the synthetic web.
     */
    private Graph nodeGraph;

    protected String accessUriTemplates[];
    protected String resourceUriTemplates[];

    protected int getNodeId() {
        return getNodeId(generatorId);
    }

    private int getNodeId(int generatorId) {
        if (type == Types.RDF_GRAPH_GENERATOR) {
            return generatorId - 1;
        }
        throw new IllegalStateException();
    }

    private void ConsumeDataGeneratorsExchange() throws IOException {
        String queueName = dataGeneratorsChannel.queueDeclare().getQueue();
        dataGeneratorsChannel.queueBind(queueName, dataGeneratorsExchange, "");
        Consumer consumer = new GraphConsumer(dataGeneratorsChannel) {
            @Override
            public boolean filter(int id, int type) {
                return id != generatorId;
            }

            @Override
            public void handleNodeGraph(int senderId, Graph g) {
                nodeGraph = g;
                LOGGER.info("Got the node graph.");
                nodeGraphReceivedMutex.release();
            }

            @Override
            public void handleRdfGraph(int senderId, GraphMetadata gm) {
                try {
                    nodeGraphProcessedMutex.acquire();
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted", e);
                }
                int senderNodeId = getNodeId(senderId);
                if (rdfMetadata.containsKey(senderNodeId)) {
                    LOGGER.info("Got the rdf graph metadata for node {}", senderNodeId);
                    rdfMetadata.put(senderNodeId, gm);
                    targetMetadataReceivedSemaphore.release();
                }
            }
        };

        dataGeneratorsChannel.basicConsume(queueName, true, consumer);
    }

    protected void addInterlinks(GraphBuilder g, int numberOfnodesLinks) {
        int numberOfInternalNodes = g.getNumberOfIriNodes();
        Random random = new Random(seedGenerator.getNextSeed());
        for (Map.Entry<Integer, GraphMetadata> entry : rdfMetadata.entrySet()) {
            int targetNodeGraph = entry.getKey();
            GraphMetadata gm = entry.getValue();

            if (gm.entranceNodes.length == 0) {
                throw new IllegalStateException("Node " + getNodeId() + " needs to link to node " + targetNodeGraph
                        + " but there are no entrypoints.");
            }
            for (int entranceNode : gm.entranceNodes) {
                for(int i = 0; i < numberOfnodesLinks; i++) {
                    // use random node
                    int nodeWithOutgoingLink = random.nextInt(numberOfInternalNodes);
                    // add a new node
                    int externalNode = g.addNode();
                    g.setGraphIdOfNode(externalNode, targetNodeGraph, entranceNode);
                    g.setGraphId(gm.graphId);
                    int propertyId = i;
                    g.addEdge(nodeWithOutgoingLink, externalNode, propertyId);
                    LOGGER.debug("Added the edge ({}, {}, {}) where the target is node {} in graph {}.", nodeWithOutgoingLink,
                            propertyId, externalNode, entranceNode, targetNodeGraph);
                }
            }
        }
    }

    /**
     * Append a given number of Blank Nodes to an existing graph.
     * Edges are created to link to the Blank Nodes.
     *
     * @param g the graph
     * @param nodeCount
     *            the number of blank nodes
     * @param seed
     */
    private void addBlankNodes(GraphBuilder g, int nodeCount, long seed) {
        int blankNodesIndex = g.getNumberOfNodes();
        g.setBlankNodesRange(blankNodesIndex, nodeCount);
        addBNodesOrLiterals(g, blankNodesIndex, nodeCount, seed);
    }

    /**
     * Append a given number of literals to an existing graph.
     * Edges are created to link to the literals.
     *
     * @param g the graph
     * @param nodeCount
     *            the number of literals
     * @param seed
     */
    private void addLiterals(GraphBuilder g, int nodeCount, long seed) {
        int literalIndex = g.getNumberOfNodes();
        g.setLiteralsRange(literalIndex, nodeCount);
        addBNodesOrLiterals(g, literalIndex, nodeCount, seed);
    }

    private void addBNodesOrLiterals(GraphBuilder g, int index, int numberOfNodes, long seed) {
        Random generator = new Random(seed);
        int numberOfIriNodes = g.getNumberOfIriNodes();
        g.addNodes(numberOfNodes);
        for (int i = index; i < g.getNumberOfNodes(); i++) {
            //Get a random Source Node
            int sourceNode = generator.nextInt(numberOfIriNodes);
            int propertyId = 0;
            g.addEdge(sourceNode, i, propertyId);
        }
    }

    protected void sendFinalGraph(Graph g) throws Exception {
        byte[] data = SerializationHelper.serialize(SERIALIZER_CLASS, g);
        String name = String.format("graph-%0" + (int) Math.ceil(Math.log10(getNumberOfGenerators() + 1)) + "d"
                + ApiConstants.FILE_ENDING_GRAPH, getNodeId());

        // TODO: Use RabbitMQ exchange to send the data (SimpleFileSender doesn't
        // support that)
        try (InputStream is = new ByteArrayInputStream(data);
                SimpleFileSender dataSender = SimpleFileSender.create(outgoingDataQueuefactory, dataQueueName);) {
            dataSender.streamData(is, name);
        }

        try (InputStream is = new ByteArrayInputStream(data);
                SimpleFileSender dataSender = SimpleFileSender.create(outgoingDataQueuefactory, evalDataQueueName);) {
            dataSender.streamData(is, name);
        }
    }

    /**
     * Factory method to create the {@link GraphGenerator} instance used to
     * generated the node graph.
     *
     * @return the {@link GraphGenerator} instance used to generated the node graph
     */
    protected GraphGenerator createNodeGraphGenerator() {
        int[] nodetypes = Stream.of(EnvVariables.getString(ENV_NODETYPES_KEY).split(",")).mapToInt(Integer::parseInt)
                .toArray();
        boolean[] ishub = ArrayUtils.toPrimitive(Stream.of(EnvVariables.getString(ENV_ISHUB_KEY).split(","))
                .map(Boolean::parseBoolean).toArray(Boolean[]::new));
        int[][] typeconnectivity = Stream.of(EnvVariables.getString(ENV_TYPECONNECTIVITY_KEY).split(";"))
                .map(s -> Stream.of(s.split(",")).mapToInt(Integer::parseInt).toArray()).toArray(int[][]::new);
        return new RandomCloudGraph("Graph " + generatorId, nodetypes, 0, typeconnectivity);
    }

    /**
     * Factory method to create the {@link GraphGenerator} instance used to
     * generated the RDF graph.
     *
     * @return the {@link GraphGenerator} instance used to generated the RDF graph
     */
    protected GraphGenerator createRDFGraphGenerator() {
        Model benchmarkParamModel = EnvVariables.getModel(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, LOGGER);
        Resource method = RdfHelper.getObjectResource(benchmarkParamModel, null, LDCBench.graphGenerator);
        if (LDCBench.lubmGraphGenerator.equals(method)) {
            return new LUBMbasedRDFGenerator();
        }
        return new RandomRDF("Graph " + generatorId);
    }

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();

        generatorId = getGeneratorId();
        type = Types.valueOf(EnvVariables.getString(ENV_TYPE_KEY));
        LOGGER = LoggerFactory.getLogger(DataGenerator.class + "#"
                + (type == Types.NODE_GRAPH_GENERATOR ? "nodeGraph" : "rdfGraph" + (generatorId - 1)));

        long seed = EnvVariables.getLong(ApiConstants.ENV_SEED_KEY);
        int numberOfComponents = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_COUNT_KEY);
        int componentId = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_ID_KEY);
        seedGenerator = new SequentialSeedGenerator(seed, componentId, numberOfComponents);
        seed = seedGenerator.getNextSeed();

        numberOfNodes = EnvVariables.getInt(ENV_NUMBER_OF_NODES_KEY, 0);
        int numberOfGraphs = EnvVariables.getInt(ENV_NUMBER_OF_GRAPHS_KEY, 1);
        avgDegree = Double.parseDouble(EnvVariables.getString(ENV_AVERAGE_DEGREE_KEY));
        numberOfEdges = EnvVariables.getInt(ENV_NUMBER_OF_EDGES_KEY, 0);

        LOGGER.info("Seed: {}; number of nodes: {}, average degree: {}, number of edges: {}", seed, numberOfNodes,
                avgDegree, numberOfEdges);

        // BenchmarkController and DataGenerators communication
        dataGeneratorsExchange = EnvVariables.getString(ENV_DATAGENERATOR_EXCHANGE_KEY);
        dataGeneratorsChannel = cmdQueueFactory.getConnection().createChannel();

        if (type == Types.RDF_GRAPH_GENERATOR) {
            accessUriTemplates = parseStringArray(EnvVariables.getString(ENV_ACCESS_URI_TEMPLATES_KEY, LOGGER));
            resourceUriTemplates = parseStringArray(EnvVariables.getString(ENV_RESOURCE_URI_TEMPLATES_KEY, LOGGER));

            // Queue for sending final graphs to BenchmarkController
            dataQueueName = EnvVariables.getString(ENV_DATA_QUEUE_KEY);
            evalDataQueueName = EnvVariables.getString(ApiConstants.ENV_EVAL_DATA_QUEUE_KEY);

            ConsumeDataGeneratorsExchange();
        }

        if (type == Types.NODE_GRAPH_GENERATOR) {
            LOGGER.debug("Waiting for all other generators to be ready...");
            dataGeneratorsReady.acquire(numberOfNodes);
            LOGGER.debug("All other generators are ready.");
        } else {
            sendToCmdQueue(ApiConstants.DATAGENERATOR_READY_SIGNAL);
        }

        if (type == Types.NODE_GRAPH_GENERATOR) {
            generator = createNodeGraphGenerator();
        } else {
            generator = createRDFGraphGenerator();
        }

        //initialize all graphs
        GraphBuilder graphs[] = new GraphBuilder[numberOfGraphs];
        for (int i = 0; i < numberOfGraphs; i++) {
            graphs[i] = new GrphBasedGraph();
            graphs[i].setGraphId(i);
        }

        if (type == Types.NODE_GRAPH_GENERATOR) {
            nodeGraph = graphs[0];
        }

        if (numberOfNodes != 0) {
            LOGGER.debug("Generator {} : Generating {} graphs", generatorId, numberOfGraphs);
            for (int i = 0; i < graphs.length; i++) {
                LOGGER.debug("Generator {} : Generating a graph with {} nodes {} average degree and {} seed", generatorId,
                        numberOfNodes, avgDegree, seed);
                seed = seedGenerator.getNextSeed();
            	generator.generateGraph(numberOfNodes/numberOfGraphs, avgDegree, seed, graphs[i]);
            }
        } else {
            LOGGER.debug("Generator {} : Generating a graph with {} average degree and {} edges and {} seed",
                    generatorId, avgDegree, numberOfEdges, seed);
            for (int i = 0; i < graphs.length; i++) {
            	generator.generateGraph(avgDegree, numberOfEdges, seed, graphs[i]);
                seed = seedGenerator.getNextSeed();
            }
        }

        if (type == Types.RDF_GRAPH_GENERATOR) {
            seed = seedGenerator.getNextSeed();
            blankNodesRatio = Double.parseDouble(EnvVariables.getString(ENV_BLANK_NODES_RATIO));
            for (int i = 0; i < graphs.length; i++) {
                addBlankNodes(graphs[i], (int) Math.ceil(graphs[i].getNumberOfNodes() * blankNodesRatio),
                        seed);
            }
        }

        if (type == Types.NODE_GRAPH_GENERATOR) {
            LOGGER.info("Node types generated: {}", Arrays.toString(((RandomCloudGraph) generator).getNodeTypes()));
            LOGGER.debug("Broadcasting the node graph...");
        } else {
            LOGGER.debug("Waiting for the node graph...");
            nodeGraphReceivedMutex.acquire();
            LOGGER.debug("Broadcasting the rdf graph metadata...", getNodeId());
        }

        ByteBuffer header = ByteBuffer.allocate(2 * (Integer.SIZE / Byte.SIZE));
        header.putInt(generatorId);
        header.putInt(type.ordinal());

        if (type == Types.NODE_GRAPH_GENERATOR) {
            // Broadcast our graph.
            byte[] data = SerializationHelper.serialize(SERIALIZER_CLASS, nodeGraph);
            ByteBuffer buf = ByteBuffer.allocate(header.capacity() + data.length);
            buf.put(header.array());
            buf.put(data);
            dataGeneratorsChannel.basicPublish(dataGeneratorsExchange, "", null, buf.array());
        } else {
            // Broadcast the graph's metadata for every graph.
            for (GraphBuilder mygraph : graphs) {
                GraphMetadata gm = new GraphMetadata();
                gm.numberOfNodes = mygraph.getNumberOfNodes();
                gm.entranceNodes = mygraph.getEntranceNodes();
                gm.graphId = mygraph.getGraphId();

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                buf.write(header.array(), 0, header.capacity());
                ObjectOutputStream output = new ObjectOutputStream(buf);
                output.writeObject(gm);

                dataGeneratorsChannel.basicPublish(dataGeneratorsExchange, "", null, buf.toByteArray());
            }
        }

        if (type == Types.RDF_GRAPH_GENERATOR) {
            // Identify nodes linked from this node.
            rdfMetadata = Arrays.stream(nodeGraph.outgoingEdgeTargets(getNodeId())).boxed().collect(HashMap::new,
                    (m, v) -> m.put(v, null), HashMap::putAll);

            LOGGER.info("Waiting for {} rdf graphs relevant to this node... (graphs: {})", rdfMetadata.size(),
                    rdfMetadata.keySet());
            nodeGraphProcessedMutex.release(nodeGraph.getNumberOfNodes() - 1);
            targetMetadataReceivedSemaphore.acquire(rdfMetadata.size());

            LOGGER.info("Got all relevant rdf graphs.", generatorId);
<<<<<<< HEAD
=======
            int numberOfnodesLinks = Integer.parseInt(EnvVariables.getString(ENV_NUMBER_OF_NODES_LINKS));
            addInterlinks(graph, numberOfnodesLinks);
>>>>>>> develop

            for (int i = 0; i < numberOfGraphs; i++) {
                addInterlinks(graphs[i]);
            }

            // Send the final graph(s) data.
            blankNodesRatio = Double.parseDouble(EnvVariables.getString(ENV_BLANK_NODES_RATIO));
            addBlankNodes(graph, (int) Math.ceil(graph.getNumberOfNodes() * blankNodesRatio),
                    seedGenerator.getNextSeed());
            literalsRatio = Double.parseDouble(EnvVariables.getString(ENV_LITERALS_RATIO));
            addLiterals(graph, (int) Math.ceil(graph.getNumberOfNodes() * literalsRatio),
                    seedGenerator.getNextSeed());

            // Send the final graph data.
            LOGGER.info("Sending the final rdf graph data...");
            for(GraphBuilder g: graphs)
                sendFinalGraph(g);
        }

        LOGGER.debug("Generation done.", generatorId);
    }

    /**
     * Parses the given string assuming that it has been generated with
     * {@link Arrays#toString()}.
     *
     * @param envValue the string containing the array
     * @return the parsed String array
     */
    protected static String[] parseStringArray(String envValue) {
        if (envValue.length() > 2) {
            return envValue.substring(1, envValue.length() - 1).split(", ");
        } else {
            return new String[0];
        }
    }

    @Override
    protected void generateData() throws Exception {
        // Nothing to do; everything is done within the init() method
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        switch (command) {
        case ApiConstants.DATAGENERATOR_READY_SIGNAL:
            dataGeneratorsReady.release();
        }

        super.receiveCommand(command, data);
    }
}
