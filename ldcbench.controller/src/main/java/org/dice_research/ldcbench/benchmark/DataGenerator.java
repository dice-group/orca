package org.dice_research.ldcbench.benchmark;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.RandomRDF;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.dice_research.ldcbench.graph.serialization.DumbSerializer;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dice_research.ldcbench.Constants.*;

public class DataGenerator extends AbstractDataGenerator {
    public static final String ENV_TYPE_KEY = "LDCBENCH_DATAGENERATOR_TYPE";
    public static final String ENV_SEED_KEY = "LDCBENCH_DATAGENERATOR_SEED";
    public static final String ENV_NUMBER_OF_NODES_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_NODES";
    public static final String ENV_AVERAGE_DEGREE_KEY = "LDCBENCH_DATAGENERATOR_AVERAGE_DEGREE";
    public static final String ENV_NUMBER_OF_EDGES_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_EDGES";
    public static final String ENV_DATA_QUEUE_KEY = "LDCBENCH_DATA_QUEUE";
    public static final String ENV_DATAGENERATOR_EXCHANGE_KEY = "LDCBENCH_DATAGENERATOR_EXCHANGE";

    public static enum types {
        NODE_GRAPH_GENERATOR,
        RDF_GRAPH_GENERATOR
    };

    private Semaphore nodeGraphMutex = new Semaphore(0);

    private static final Class<DumbSerializer> serializerClass = DumbSerializer.class;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    private int generatorId;

    private int seed;
    private types type;
    private int numberOfNodes;
    private double avgDegree;
    private int numberOfEdges;

    private SimpleFileSender dataSender;
    private Channel dataGeneratorsChannel;
    private String dataGeneratorsExchange;

    private Graph nodeGraph;

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();

        generatorId = getGeneratorId();
        seed = EnvVariables.getInt(ENV_SEED_KEY);
        type = types.valueOf(EnvVariables.getString(ENV_TYPE_KEY));
        numberOfNodes = EnvVariables.getInt(ENV_NUMBER_OF_NODES_KEY, 0);
        avgDegree = Double.parseDouble(EnvVariables.getString(ENV_AVERAGE_DEGREE_KEY));
        numberOfEdges = EnvVariables.getInt(ENV_NUMBER_OF_EDGES_KEY, 0);

        LOGGER.info("Seed: {}", seed);

        // BenchmarkController and DataGenerators communication
        dataGeneratorsExchange = EnvVariables.getString(ENV_DATAGENERATOR_EXCHANGE_KEY);
        dataGeneratorsChannel = cmdQueueFactory.getConnection().createChannel();
        String queueName = dataGeneratorsChannel.queueDeclare().getQueue();
        dataGeneratorsChannel.queueBind(queueName, dataGeneratorsExchange, "");
        Consumer consumer = new GraphConsumer(dataGeneratorsChannel) {
            @Override
            public boolean filter(int id, int type) {
                return id != generatorId;
            }
            @Override
            public void handleNodeGraph(Graph g) {
                nodeGraph = g;
                LOGGER.info("Got the node graph");
                nodeGraphMutex.release();
            }
        };
        if (type == types.RDF_GRAPH_GENERATOR) {
            dataGeneratorsChannel.basicConsume(queueName, true, consumer);
        }

        // Queue for sending final graphs to BenchmarkController
        if (type == types.RDF_GRAPH_GENERATOR) {
            String dataQueueName = EnvVariables.getString(ENV_DATA_QUEUE_KEY);
            dataSender = SimpleFileSender.create(outgoingDataQueuefactory, dataQueueName);
        }

        LOGGER.info("DataGenerator initialized (ID: {}, type: {})", generatorId, type);
    }

    @Override
    protected void generateData() throws Exception {
        GraphGenerator generator = new RandomRDF("Graph " + generatorId);
        GraphBuilder graph = new GrphBasedGraph();

        if (type == types.NODE_GRAPH_GENERATOR) {
            nodeGraph = graph;
        }

        if (numberOfNodes != 0) {
            LOGGER.debug("Generating a graph with {} nodes and {} average degree", numberOfNodes, avgDegree);
            generator.generateGraph(numberOfNodes, avgDegree, seed, graph);
        } else {
            LOGGER.debug("Generating a graph with {} average degree and {} edges", avgDegree, numberOfEdges);
            generator.generateGraph(avgDegree, numberOfEdges, seed, graph);
        }

        if (type == types.NODE_GRAPH_GENERATOR) {
            LOGGER.debug("Broadcasting the node graph...");
            byte[] data = SerializationHelper.serialize(serializerClass, nodeGraph);
            ByteBuffer buf = ByteBuffer.allocate(2 * (Integer.SIZE / Byte.SIZE) + data.length);
            buf.putInt(generatorId);
            buf.putInt(type.ordinal());
            buf.put(data);
            dataGeneratorsChannel.basicPublish(dataGeneratorsExchange, "", null, buf.array());
        } else {
            LOGGER.debug("Waiting for the node graph...");
            nodeGraphMutex.acquire();

            // TODO

            // Send the final graph data.
            LOGGER.info("Sending the graph data to the node...");
            dataSender.streamData(new ByteArrayInputStream(SerializationHelper.serialize(serializerClass, graph)), "");
        }

        LOGGER.debug("Generator {}: Generation done.", generatorId);
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        LOGGER.debug("close()");
        // Always close the super class after yours!
        super.close();
    }
}
