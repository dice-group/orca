package org.dice_research.ldcbench.nodes.components;

import org.hobbit.core.Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.io.IOUtils;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.rabbit.GraphHandler;
import org.dice_research.ldcbench.rabbit.ObjectStreamFanoutExchangeConsumer;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.rabbit.DataReceiver;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class NodeComponent extends AbstractCommandReceivingComponent implements AbstractNodeComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNodeComponent.class);

    protected Model benchmarkParamModel;
    protected SeedGenerator seedGenerator;

    protected Semaphore dataGenerationFinished = new Semaphore(0);
    protected Semaphore nodeAcked = new Semaphore(0);
    protected SettableFuture<Integer> cloudNodeId = SettableFuture.create();
    protected String nodeURI;
    protected boolean dockerized;

    /**
     * Must be set during initBeforeDataGeneration, would be used as a hostname
     * by which this node should be accessed by the benchmarked system
     * instead of node container's hostname.
     */
    protected String resourceUriTemplate;
    protected String accessUriTemplate;

    protected DataReceiver receiver;
    protected ObjectStreamFanoutExchangeConsumer<NodeMetadata[]> bcBroadcastConsumer;
    protected NodeMetadata nodeMetadata[];
    protected List<Graph> graphs;

    @Override
    public void init() throws Exception {
        super.init();

        benchmarkParamModel = EnvVariables.getModel(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, LOGGER);
        long seed = EnvVariables.getLong(ApiConstants.ENV_SEED_KEY);
        int numberOfComponents = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_COUNT_KEY);
        int componentId = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_ID_KEY);
        seedGenerator = new SequentialSeedGenerator(seed, componentId, numberOfComponents);

        cloudNodeId.set(EnvVariables.getInt(ApiConstants.ENV_NODE_ID_KEY, LOGGER));
        dockerized = EnvVariables.getBoolean(ApiConstants.ENV_DOCKERIZED_KEY, true, LOGGER);
        nodeURI = EnvVariables.getString(ApiConstants.ENV_NODE_URI_KEY, LOGGER);

        // initialize exchange with BC
        String exchangeName = EnvVariables.getString(ApiConstants.ENV_BENCHMARK_EXCHANGE_KEY);
        bcBroadcastConsumer = new ObjectStreamFanoutExchangeConsumer<NodeMetadata[]>(cmdQueueFactory, exchangeName) {
            @Override
            public void handle(NodeMetadata[] body) {
                try {
                    handleBCMessage(body);
                } catch (Exception e) {
                    LOGGER.error("Exception while trying to handle incoming command.", e);
                }
            }
        };

        // initialize graph queue
        String queueName = EnvVariables.getString(ApiConstants.ENV_DATA_QUEUE_KEY);
        SimpleFileReceiver receiver = SimpleFileReceiver.create(this.incomingDataQueueFactory, queueName);
        GraphHandler graphHandler = new GraphHandler(receiver);
        Thread receiverThread = new Thread(graphHandler);
        receiverThread.start();

        sendToCmdQueue(ApiConstants.NODE_START_SIGNAL, RabbitMQUtils.writeLong(cloudNodeId.get()));

        initBeforeDataGeneration();

        if (resourceUriTemplate == null || accessUriTemplate == null) {
            throw new IllegalStateException("URI templates are not set.");
        }

        nodeAcked.acquire();
        sendToCmdQueue(ApiConstants.NODE_URI_TEMPLATE, RabbitMQUtils.writeByteArrays(new byte[][] {
            RabbitMQUtils.writeString(Integer.toString(cloudNodeId.get())),
            RabbitMQUtils.writeString(resourceUriTemplate),
            RabbitMQUtils.writeString(accessUriTemplate),
        }));
        LOGGER.debug("{} initialized.", this);
        sendToCmdQueue(ApiConstants.NODE_INIT_SIGNAL);

        // Wait for the data generation to finish
        dataGenerationFinished.acquire();

        receiver.terminate();
        receiverThread.join();

        if (graphHandler.encounteredError()) {
            throw new IllegalStateException("Encountered an error while receiving graphs.");
        }
        graphs = graphHandler.getGraphs();
        if (graphs.isEmpty()) {
            throw new IllegalStateException("Didn't receive a single graph.");
        }
        if (nodeMetadata == null) {
            throw new IllegalStateException("Didn't receive the URI templates from the benchmark controller.");
        }

        initAfterDataGeneration();

        LOGGER.debug("{} is ready.", this);
        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        switch (command) {
        case Commands.DATA_GENERATION_FINISHED:
            LOGGER.debug("Received DATA_GENERATION_FINISHED");
            dataGenerationFinished.release();
        break; case ApiConstants.NODE_ACK_SIGNAL:
            try {
                if (RabbitMQUtils.readLong(data) == cloudNodeId.get()) {
                    nodeAcked.release();
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        break; case ApiConstants.CRAWLING_FINISHED_SIGNAL:
            Model nodeResult = ModelFactory.createDefaultModel();
            Resource root = nodeResult.createResource(nodeURI);
            addResults(nodeResult, root);
            try {
                sendToCmdQueue(ApiConstants.NODE_RESULTS_SIGNAL, RabbitMQUtils.writeModel(nodeResult));
            } catch (IOException e) {
                LOGGER.error("Failed to write node result model.", e);
                throw new IllegalStateException(e);
            }
        }
    }

    protected void handleBCMessage(NodeMetadata[] nodeMetadata) {
        if (nodeMetadata != null) {
            this.nodeMetadata = nodeMetadata;
        } else {
            LOGGER.error("Couldn't parse node metadata received from benchmark controller.");
            this.nodeMetadata = null;
        }
        LOGGER.debug("Got node metadata: {}", Arrays.toString(this.nodeMetadata));
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(receiver);
        if (bcBroadcastConsumer != null) {
            bcBroadcastConsumer.close();
        }
        super.close();
    }

    @Override
    public void run() throws Exception {
        synchronized (this) {
            this.wait();
        }
    }

    @Override
    public String toString() {
        return "Node " + cloudNodeId + " (" + getClass().getName() + ")";
    }
}
