package org.dice_research.ldcbench.nodes.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.IOUtils;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
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

abstract public class AbstractNodeComponent extends AbstractCommandReceivingComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNodeComponent.class);

    protected Semaphore dataGenerationFinished = new Semaphore(0);
    protected int cloudNodeId;

    /**
     * If set to non-null during initBeforeDataGeneration, would be used as a hostname
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

        cloudNodeId = EnvVariables.getInt(ApiConstants.ENV_NODE_ID_KEY, LOGGER);

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

        initBeforeDataGeneration();

        if (resourceUriTemplate != null && accessUriTemplate != null) {
            sendToCmdQueue(ApiConstants.NODE_URI_TEMPLATE, RabbitMQUtils.writeByteArrays(new byte[][] {
                RabbitMQUtils.writeString(Integer.toString(cloudNodeId)),
                RabbitMQUtils.writeString(resourceUriTemplate),
                RabbitMQUtils.writeString(accessUriTemplate),
            }));
        }

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

    /**
     * Initialization before any data is generated should be performed there.
     * If needed, resourceUriTemplate and accessUriTemplate should be set there,
     * possibly after starting relevant containers.
     */
    abstract public void initBeforeDataGeneration() throws Exception;

    /**
     * Initialization with data generation already done.
     * nodeMetadata[] (for all nodes) and graphs are available at this point.
     */
    abstract public void initAfterDataGeneration() throws Exception;

    @Override
    public void receiveCommand(byte command, byte[] data) {
        switch (command) {
        case Commands.DATA_GENERATION_FINISHED:
            LOGGER.debug("Received DATA_GENERATION_FINISHED");
            dataGenerationFinished.release();
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
