package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.eval.CrawledDataEvaluator;
import org.dice_research.ldcbench.benchmark.eval.EvaluationResult;
import org.dice_research.ldcbench.benchmark.eval.FileBasedGraphSupplier;
import org.dice_research.ldcbench.benchmark.eval.GraphSupplier;
import org.dice_research.ldcbench.benchmark.eval.GraphValidator;
import org.dice_research.ldcbench.benchmark.eval.SimpleCompleteEvaluator;
import org.dice_research.ldcbench.benchmark.eval.SparqlBasedTripleCounter;
import org.dice_research.ldcbench.benchmark.eval.SparqlBasedValidator;
import org.dice_research.ldcbench.benchmark.eval.TripleCountingTimerTask;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.rabbit.ObjectStreamFanoutExchangeConsumer;
import org.dice_research.ldcbench.rabbit.SimpleFileQueueConsumer;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.dice_research.ldcbench.vocab.LDCBenchDiagrams;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.EnvVariables;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalModule extends AbstractCommandReceivingComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvalModule.class);

    protected static final long PERIOD_FOR_TRIPLE_COUNTER = 60000;

    /**
     * The URI of the experiment.
     */
    protected String experimentUri;
    protected String sparqlEndpoint;

    protected ObjectStreamFanoutExchangeConsumer<NodeMetadata[]> bcBroadcastConsumer;

    protected SimpleFileQueueConsumer graphConsumer;

    protected Semaphore crawlingFinished = new Semaphore(0);
    protected Semaphore dataGenerationFinished = new Semaphore(0);

    protected String graphFiles[];
    protected String domainNames[];
    protected long startTimeStamp;
    protected long endTimeStamp;
    protected Timer timer = null;
    protected TripleCountingTimerTask countingTimerTask = null;

    @Override
    public void init() throws Exception {
        super.init();

        // Get the experiment URI
        experimentUri = EnvVariables.getString(Constants.HOBBIT_EXPERIMENT_URI_KEY, LOGGER);

        // Get the sparql endpoint
        sparqlEndpoint = EnvVariables.getString(ApiConstants.ENV_SPARQL_ENDPOINT_KEY, LOGGER);

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
                Arrays.sort(files);
                LOGGER.debug("Got files: {}", Arrays.toString(files));
                graphFiles = files;
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
    }

    @Override
    public void run() throws Exception {
        // Let the BC now that this module is ready
        sendToCmdQueue(Commands.EVAL_MODULE_READY_SIGNAL);

        // Wait for all the graphs to be sent
        dataGenerationFinished.acquire();
        graphConsumer.close();
        graphConsumer = null;

        LOGGER.info("Waiting for the crawling to finish...");
        crawlingFinished.acquire();

        // Evaluate the crawled data, create result model and terminate
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
            break;
        case ApiConstants.CRAWLING_STARTED_SIGNAL:
            crawlingStarted(RabbitMQUtils.readLong(data));
            break;
        case ApiConstants.CRAWLING_FINISHED_SIGNAL:
            crawlingEnded(RabbitMQUtils.readLong(data));
            break;
        default: // nothing to do
        }
    }

    @Override
    public void close() {
        // Free the resources you requested here
        if (bcBroadcastConsumer != null) {
            bcBroadcastConsumer.close();
        }
        if (graphConsumer != null) {
            graphConsumer.close();
        }
        if (timer != null) {
            timer.cancel();
        }
        // Always close the super class after yours!
        try {
            super.close();
        } catch (Exception e) {
        }
    }

    protected void crawlingStarted(long readLong) {
        LOGGER.debug("Crawling started at {}", new Date(readLong));
        startTimeStamp = readLong;
        try {
            countingTimerTask = new TripleCountingTimerTask(SparqlBasedTripleCounter.create(sparqlEndpoint));
            countingTimerTask.getTimestamps().add(startTimeStamp);
            countingTimerTask.getTripleCounts().add(0L);
            timer = new Timer();
            timer.schedule(countingTimerTask, (System.currentTimeMillis() + PERIOD_FOR_TRIPLE_COUNTER) - startTimeStamp,
                    PERIOD_FOR_TRIPLE_COUNTER);
        } catch (Exception e) {
            LOGGER.error("Error while starting the timer task for counting triples.", e);
        }
    }

    protected void crawlingEnded(long readLong) {
        LOGGER.debug("Crawling finished at {}", new Date(readLong));
        endTimeStamp = readLong;
        // Stop the timer if it exists
        if (timer != null) {
            timer.cancel();
            // Execute the counter one last time
            countingTimerTask.run();
        }
        crawlingFinished.release();
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
        // Evaluate the results based on the data from the SPARQL storage
        GraphSupplier supplier = new FileBasedGraphSupplier(graphFiles, domainNames);
        GraphValidator validator = SparqlBasedValidator.create(sparqlEndpoint);
        CrawledDataEvaluator evaluator = new SimpleCompleteEvaluator(supplier, validator);
        return evaluator.evaluate();
    }

    protected Model summarizeEvaluation(EvaluationResult result) throws Exception {
        // Write results into a Jena model and send it to the BC
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(experimentUri), RDF.type, HOBBIT.Experiment);
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(model.createLiteralStatement(experimentResource, LDCBench.triplesEvaluated, result.checkedTriples));
        model.add(model.createLiteralStatement(experimentResource, LDCBench.truePositives, result.truePositives));
        model.add(model.createLiteralStatement(experimentResource, LDCBench.recall, result.recall));
        // Add runtime if > 0
        long runtime = endTimeStamp - startTimeStamp;
        if (runtime > 0) {
            model.add(model.createLiteralStatement(experimentResource, LDCBench.runtime, runtime));
        }

        // Transform the data of the triple counter into RDF
        if ((countingTimerTask != null) && (countingTimerTask.getTimestamps().size() > 1)
                && (countingTimerTask.getTripleCounts().size() > 1)) {
            Resource dataset = LDCBenchDiagrams.createDataset(model, getHobbitSessionId());
            List<Long> timestamps = countingTimerTask.getTimestamps();
            List<Long> counts = countingTimerTask.getTripleCounts();
            for (int i = 0; i < timestamps.size(); ++i) {
                LDCBenchDiagrams.addPoint(model, dataset, getHobbitSessionId(), i, timestamps.get(i) - startTimeStamp,
                        counts.get(i));
            }
            model.add(experimentResource, LDCBench.tripleCountOverTime, dataset);
        }
        return model;
    }
}
