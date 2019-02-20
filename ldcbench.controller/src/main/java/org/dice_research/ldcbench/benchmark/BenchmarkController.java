package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.SimpleTripleCreator;

import org.apache.commons.io.IOUtils;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.ArrayUtils;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.rabbit.DataSender;
import org.hobbit.core.rabbit.DataSenderImpl;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.rdf.RdfHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dice_research.ldcbench.Constants.*;

public class BenchmarkController extends AbstractBenchmarkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkController.class);

    private String sparqlEndpoint;
    private String seedURI;

    private Semaphore nodesReadySemaphore = new Semaphore(0);
    private Semaphore nodeGraphMutex = new Semaphore(0);

    Channel dataGeneratorsChannel;

    String dataGeneratorsExchange;
    DataSender systemDataSender;
    DataSender systemTaskSender;

    private String getRandomNameForRabbitMQ() {
        return java.util.UUID.randomUUID().toString();
    }

    private void createDataGenerator(String generatorImageName, String[] envVariables) {
        String containerId;
        String variables[] = envVariables != null ? Arrays.copyOf(envVariables, envVariables.length + 1)
                : new String[1];

        variables[variables.length - 1] = Constants.GENERATOR_ID_KEY + "=" + dataGenContainerIds.size();
        containerId = createContainer(generatorImageName, variables);
        if (containerId != null) {
            dataGenContainerIds.add(containerId);
        } else {
            String errorMsg = "Couldn't create generator component. Aborting.";
            LOGGER.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    @Override
    public void init() throws Exception {
        super.init();

        String[] envVariables;

        // Start Virtuoso
        String vos = createContainer("openlink/virtuoso-opensource-7", Constants.CONTAINER_TYPE_BENCHMARK, new String[]{
            "DBA_PASSWORD=" + VOS_PASSWORD,
        });
        sparqlEndpoint = "http://" + vos + ":8890/sparql";

        // You might want to load parameters from the benchmarks parameter model
        int seed = RdfHelper.getLiteral(benchmarkParamModel, null, LDCBench.seed).getInt();
        int nodesAmount = RdfHelper.getLiteral(benchmarkParamModel, null, LDCBench.numberOfNodes).getInt();
        int triplesPerNode = RdfHelper.getLiteral(benchmarkParamModel, null, LDCBench.triplesPerNode).getInt();
        Duration averageNodeDelay = RdfHelper.getDurationValue(benchmarkParamModel, null, LDCBench.averageNodeDelay);
        int averageNodeGraphDegree = RdfHelper.getLiteral(benchmarkParamModel, null, LDCBench.averageNodeGraphDegree).getInt();
        int averageRdfGraphDegree = RdfHelper.getLiteral(benchmarkParamModel, null, LDCBench.averageRdfGraphDegree).getInt();

        // Create the other components

        dataGeneratorsChannel = cmdQueueFactory.getConnection().createChannel();
        String queueName = dataGeneratorsChannel.queueDeclare().getQueue();
        String dataGeneratorsExchange = getRandomNameForRabbitMQ();
        dataGeneratorsChannel.exchangeDeclare(dataGeneratorsExchange, "fanout", false, true, null);
        dataGeneratorsChannel.queueBind(queueName, dataGeneratorsExchange, "");

        Consumer consumer = new GraphConsumer(dataGeneratorsChannel) {
            @Override
            public void handleNodeGraph(int senderId, Graph g) {
                LOGGER.info("Got the node graph");
                nodeGraphMutex.release();
            }
        };
        dataGeneratorsChannel.basicConsume(queueName, true, consumer);

        // Exchange for broadcasting metadata to nodes
        String benchmarkExchange = getRandomNameForRabbitMQ();
        dataGeneratorsChannel.exchangeDeclare(benchmarkExchange, "fanout", false, true, null);

        // Greate queues for sending data to nodes
        String[] dataQueues = new String[nodesAmount];
        for (int i = 0; i < nodesAmount; i++) {
            dataQueues[i] = dataGeneratorsChannel.queueDeclare(getRandomNameForRabbitMQ(), false, false, true, null)
                    .getQueue();
        }

        LOGGER.debug("Starting all cloud nodes...");
        NodeMetadata[] nodeMetadata = new NodeMetadata[nodesAmount];
        for (int i = 0; i < nodesAmount; i++) {
            envVariables = new String[] {
                    ApiConstants.ENV_NODE_ID_KEY + "=" + i,
                    ApiConstants.ENV_BENCHMARK_EXCHANGE_KEY + "=" + benchmarkExchange,
                    ApiConstants.ENV_DATA_QUEUE_KEY + "=" + dataQueues[i],
                    ApiConstants.ENV_NODE_DELAY_KEY + "=" + averageNodeDelay.toMillis(),
            };

            String containerId = createContainer(HTTPNODE_IMAGE_NAME, Constants.CONTAINER_TYPE_BENCHMARK, envVariables);

            nodeMetadata[i] = new NodeMetadata();
            nodeMetadata[i].setHostname(containerId);
            // FIXME: HOBBIT SDK workaround (setting environment for "containers")
            Thread.sleep(2000);
        }

        // FIXME use entrance node of the node graph instead of 0
        SimpleTripleCreator tripleCreator = new SimpleTripleCreator(0, Stream.of(nodeMetadata).map(nm -> nm.getHostname()).toArray(String[]::new));
        // FIXME use one of entrance nodes in graph instead of 0
        seedURI = tripleCreator.createNode(0, -1, -1, false).toString();
        LOGGER.info("Seed URI: {}", seedURI);

        String evalDataQueueName = getRandomNameForRabbitMQ();
        LOGGER.debug("Creating evaluation module...");
        createEvaluationModule(EVALMODULE_IMAGE_NAME, new String[] {
            ApiConstants.ENV_BENCHMARK_EXCHANGE_KEY + "=" + benchmarkExchange,
            ApiConstants.ENV_EVAL_DATA_QUEUE_KEY + "=" + evalDataQueueName,
            ApiConstants.ENV_SPARQL_ENDPOINT_KEY + "=" + sparqlEndpoint,
        });

        LOGGER.debug("Waiting for all cloud nodes and evaluation module to be ready...");
        nodesReadySemaphore.acquire(nodesAmount + 1);

        LOGGER.debug("Broadcasting metadata to cloud nodes...");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        new ObjectOutputStream(buf).writeObject(nodeMetadata);
        dataGeneratorsChannel.basicPublish(benchmarkExchange, "", null, buf.toByteArray());

        LOGGER.debug("Creating data generators...");

        SeedGenerator seedGenerator = new SeedGenerator(seed);

        // Node graph generator
        envVariables = new String[] {
                DataGenerator.ENV_TYPE_KEY + "=" + DataGenerator.Types.NODE_GRAPH_GENERATOR,
                DataGenerator.ENV_SEED_KEY + "=" + seedGenerator.applyAsInt(0),
                DataGenerator.ENV_NUMBER_OF_NODES_KEY + "=" + nodesAmount,
                DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + averageNodeGraphDegree,
                DataGenerator.ENV_DATAGENERATOR_EXCHANGE_KEY + "=" + dataGeneratorsExchange, };
        createDataGenerators(DATAGEN_IMAGE_NAME, 1, envVariables);
        // FIXME: HOBBIT SDK workaround (setting environment for "containers")
        Thread.sleep(2000);

        // RDF graph generators
        for (int i = 0; i < nodesAmount; i++) {
            envVariables = new String[] {
                    Constants.GENERATOR_COUNT_KEY + "=" + nodesAmount,
                    DataGenerator.ENV_TYPE_KEY + "=" + DataGenerator.Types.RDF_GRAPH_GENERATOR,
                    DataGenerator.ENV_SEED_KEY + "=" + seedGenerator.applyAsInt(1 + i),
                    DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + averageRdfGraphDegree,
                    DataGenerator.ENV_NUMBER_OF_EDGES_KEY + "=" + triplesPerNode,
                    DataGenerator.ENV_DATA_QUEUE_KEY + "=" + dataQueues[i],
                    ApiConstants.ENV_EVAL_DATA_QUEUE_KEY + "=" + evalDataQueueName,
                    DataGenerator.ENV_DATAGENERATOR_EXCHANGE_KEY + "=" + dataGeneratorsExchange, };
            createDataGenerator(DATAGEN_IMAGE_NAME, envVariables);
            // FIXME: HOBBIT SDK workaround (setting environment for "containers")
            Thread.sleep(2000);
        }

        LOGGER.debug("Waiting for components to initialize...");
        waitForComponentsToInitialize();
    }

    @Override
    protected void executeBenchmark() throws Exception {
        LOGGER.debug("BenchmarkController.executeBenchmark()");
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

        LOGGER.debug("Waiting for the node graph...");
        nodeGraphMutex.acquire();

        LOGGER.debug("Waiting for the data generators to finish...");
        waitForDataGenToFinish();

        LOGGER.debug("Sending information to the system...");
        systemDataSender = DataSenderImpl.builder().queue(getFactoryForOutgoingDataQueues(),
                generateSessionQueueName(Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME)).build();
        systemDataSender.sendData(RabbitMQUtils.writeString(sparqlEndpoint));
        systemDataSender.closeWhenFinished();

        systemTaskSender = DataSenderImpl.builder().queue(getFactoryForOutgoingDataQueues(),
                generateSessionQueueName(Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME)).build();
        systemTaskSender.sendData(
                RabbitMQUtils.writeByteArrays(new byte[][] { RabbitMQUtils.writeString("0"), RabbitMQUtils.writeString(seedURI) }));
        systemTaskSender.closeWhenFinished();

        sendToCmdQueue(ApiConstants.CRAWLING_STARTED_SIGNAL, RabbitMQUtils.writeLong(new Date().getTime()));

        LOGGER.debug("Waiting for the system to finish...");
        waitForSystemToFinish();

        sendToCmdQueue(ApiConstants.CRAWLING_FINISHED_SIGNAL, RabbitMQUtils.writeLong(new Date().getTime()));

        waitForEvalComponentsToFinish();

        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);

        // Send the resultModul to the platform controller and terminate
        LOGGER.debug("Sending result model: {}", RabbitMQUtils.writeModel2String(resultModel));
        sendResultModel(resultModel);
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        switch (command) {
        case ApiConstants.NODE_READY_SIGNAL:
            LOGGER.debug("Received NODE_READY_SIGNAL");
            nodesReadySemaphore.release();
        }

        super.receiveCommand(command, data);
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("BenchmarkController.close()");
        // Free the resources you requested here
        IOUtils.closeQuietly(systemDataSender);
        IOUtils.closeQuietly(systemTaskSender);

        // Always close the super class after yours!
        super.close();
    }
}
