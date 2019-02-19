package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.eval.EvaluationResult;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.rabbit.ObjectStreamFanoutExchangeConsumer;
import org.dice_research.ldcbench.rabbit.SimpleFileQueueConsumer;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.utils.EnvVariables;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalModule extends AbstractCommandReceivingComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvalModule.class);

    /**
     * The URI of the experiment.
     */
    protected String experimentUri;
    protected String sparqlEndpoint;

    protected ObjectStreamFanoutExchangeConsumer<NodeMetadata[]> bcBroadcastConsumer;

    protected SimpleFileQueueConsumer graphConsumer;

    protected Semaphore dataGenerationFinished = new Semaphore(0);

    protected String domainNames[];
    protected Semaphore domainNamesReceived = new Semaphore(0);

    @Override
    public void init() throws Exception {
        super.init();

        // Get the experiment URI
        experimentUri = EnvVariables.getString(Constants.HOBBIT_EXPERIMENT_URI_KEY, LOGGER);

        // initialize exchange with BC
        String exchangeName = EnvVariables.getString(ApiConstants.ENV_BENCHMARK_EXCHANGE_KEY);
        bcBroadcastConsumer = new ObjectStreamFanoutExchangeConsumer<NodeMetadata[]>(cmdQueueFactory, exchangeName) {
            @Override
            public void handle(NodeMetadata[] body) {
                try {
                    handleBCMessage(body);
                } catch (Exception e) {
                    LOGGER.error("Exception while trying to handle incoming message.", e);
                }
            }
        };

        String graphQueueName = EnvVariables.getString(ApiConstants.ENV_EVAL_DATA_QUEUE_KEY);
        graphConsumer = new SimpleFileQueueConsumer(incomingDataQueueFactory, graphQueueName) {
            @Override
            public void handle(String[] files) {
                LOGGER.debug("Got files: {}", Arrays.toString(files));
            }
        };

        // Signal to the BC that we are ready to receive
        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);

        LOGGER.info("Evaluation module initialized.");
    }

    protected void handleBCMessage(NodeMetadata[] nodeMetadata) {
        if (nodeMetadata != null) {
            domainNames = new String[nodeMetadata.length];
            for (int i = 0; i < nodeMetadata.length; ++i) {
                domainNames[i] = nodeMetadata[i].getHostname();
            }
        } else {
            LOGGER.error("Couldn't parse node metadata received from benchmark controller.");
            domainNames = null;
        }
        LOGGER.debug("Got domain names: {}", Arrays.toString(domainNames));
        // In any case, we should release the semaphore. Otherwise, this component would
        // get stuck and wait forever for an additional message.
        domainNamesReceived.release();
    }

    @Override
    public void run() throws Exception {
        // Let the BC now that this module is ready
        sendToCmdQueue(Commands.EVAL_MODULE_READY_SIGNAL);

        // Wait for all the graphs to be sent
        dataGenerationFinished.acquire();
        graphConsumer.close();
        graphConsumer = null;

        // TODO wait for the crawling to finish

        // TODO evaluate the results based on the data from the SPARQL storage
        // Create result model and terminate
        Model model = summarizeEvaluation(runEvaluation());
        LOGGER.info("The result model has " + model.size() + " triples.");
        sendResultModel(model);
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        switch (command) {
        case Commands.DATA_GENERATION_FINISHED:
            LOGGER.debug("Received DATA_GENERATION_FINISHED");
            dataGenerationFinished.release();
        }
    }

    @Override
    public void close(){
        // Free the resources you requested here
        if (bcBroadcastConsumer != null) {
            bcBroadcastConsumer.close();
        }
        if (graphConsumer != null) {
            graphConsumer.close();
        }
        // Always close the super class after yours!
        try {
            super.close();
        }
        catch (Exception e){
        }
    }

    /**
     * Sends the model to the benchmark controller.
     *
     * @param model
     *            the model that should be sent
     * @throws IOException
     *             if an error occurs during the commmunication
     */
    private void sendResultModel(Model model) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "JSONLD");
        sendToCmdQueue(Commands.EVAL_MODULE_FINISHED_SIGNAL, outputStream.toByteArray());
    }

    private EvaluationResult runEvaluation() {
        
        return null;
    }

    protected Model summarizeEvaluation(EvaluationResult result) throws Exception {
        // All tasks/responsens have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(experimentUri), RDF.type, HOBBIT.Experiment);
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);

        return model;
    }
}
