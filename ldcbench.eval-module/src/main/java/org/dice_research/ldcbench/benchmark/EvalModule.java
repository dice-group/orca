package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.eval.CrawledDataEvaluator;
import org.dice_research.ldcbench.benchmark.eval.EvaluationResult;
import org.dice_research.ldcbench.benchmark.eval.GraphValidator;
import org.dice_research.ldcbench.benchmark.eval.SimpleCompleteEvaluator;
import org.dice_research.ldcbench.benchmark.eval.sparql.SparqlBasedTripleCounter;
import org.dice_research.ldcbench.benchmark.eval.sparql.SparqlBasedValidator;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.FileBasedTripleBlockStreamSupplier;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.TTLTarGZBasedTripleBlockStreamCreator;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.GraphBasedTripleBlockStreamCreator;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.SamplingTripleBlockStreamDecorator;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.TripleBlockStreamSupplier;
import org.dice_research.ldcbench.benchmark.eval.timer.ResourceUsageTimerTask;
import org.dice_research.ldcbench.benchmark.eval.timer.TripleCountingTimerTask;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;
import org.dice_research.ldcbench.rabbit.ObjectStreamFanoutExchangeConsumer;
import org.dice_research.ldcbench.rabbit.SimpleFileQueueConsumer;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.dice_research.ldcbench.vocab.LDCBenchDiagrams;
import org.dice_research.ldcbench.vocab.ResourceUsageDiagrams;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.core.components.utils.SystemResourceUsageRequester;
import org.hobbit.core.data.usage.ResourceUsageInformation;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.EnvVariables;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalModule extends AbstractPlatformConnectorComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvalModule.class);

    protected static final long PERIOD_FOR_TRIPLE_COUNTER = 60000;
    protected static final long PERIOD_FOR_RESOURCE_USAGE_REQUESTER = 60000;

    private boolean sdk;

    /**
     * The URI of the experiment.
     */
    protected String experimentUri;
    protected String sparqlEndpoint;
    protected double part2Evaluate;
    /**
     * A generator of seed values to initialize random number generators.
     */
    private SeedGenerator seedGenerator;

    protected ObjectStreamFanoutExchangeConsumer<NodeMetadata[]> bcBroadcastConsumer;

    protected SimpleFileQueueConsumer graphConsumer;

    protected Semaphore crawlingFinished = new Semaphore(0);
    protected Semaphore dataGenerationFinished = new Semaphore(0);

    protected String graphFiles[];
    private NodeMetadata[] nodeMetadata;
    protected long startTimeStamp;
    protected long endTimeStamp;
    protected Timer timer = null;
    protected TripleCountingTimerTask countingTimerTask = null;
    protected ResourceUsageTimerTask resourcesTimerTask = null;

    @Override
    public void init() throws Exception {
        super.init();

        sdk = EnvVariables.getBoolean(ApiConstants.ENV_SDK_KEY, false, LOGGER);

        // Get the experiment URI
        experimentUri = EnvVariables.getString(Constants.HOBBIT_EXPERIMENT_URI_KEY, LOGGER);

        // Get the sparql endpoint
        sparqlEndpoint = EnvVariables.getString(ApiConstants.ENV_SPARQL_ENDPOINT_KEY, LOGGER);

        // Get the sparql endpoint
        String evaluatePercent = EnvVariables.getString(ApiConstants.ENV_EVALUATION_RATIO_KEY, () -> "1.0", LOGGER);
        part2Evaluate = Double.parseDouble(evaluatePercent);

        long seed = EnvVariables.getLong(ApiConstants.ENV_SEED_KEY);
        int numberOfComponents = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_COUNT_KEY);
        int componentId = EnvVariables.getInt(ApiConstants.ENV_COMPONENT_ID_KEY);
        seedGenerator = new SequentialSeedGenerator(seed, componentId, numberOfComponents);

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
        sendToCmdQueue(ApiConstants.NODE_INIT_SIGNAL);

        LOGGER.info("Evaluation module initialized.");
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
    public void run() throws Exception {
        // Let the BC now that this module is ready
        sendToCmdQueue(Commands.EVAL_MODULE_READY_SIGNAL);

        // Wait for all the graphs to be sent
        dataGenerationFinished.acquire();
        graphConsumer.close();
        graphConsumer = null;

        LOGGER.info("Waiting for the evaluation phase...");
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

            if (!sdk) {
                resourcesTimerTask = new ResourceUsageTimerTask(
                        SystemResourceUsageRequester.create(this, getHobbitSessionId()));
                timer.schedule(resourcesTimerTask,
                        System.currentTimeMillis() + PERIOD_FOR_RESOURCE_USAGE_REQUESTER - startTimeStamp,
                        PERIOD_FOR_RESOURCE_USAGE_REQUESTER);
            } else {
                LOGGER.debug("Will not request resource usage.");
            }
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
            // Do not execute the resource usage requester since now all the system
            // containers are gone.
        }
        crawlingFinished.release();
    }

    /**
     * Sends the model to the benchmark controller.
     *
     * @param model the model that should be sent
     * @throws IOException if an error occurs during the commmunication
     */
    private void sendResultModel(Model model) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "JSONLD");
        sendToCmdQueue(Commands.EVAL_MODULE_FINISHED_SIGNAL, outputStream.toByteArray());
    }

    private Map<Integer, EvaluationResult> runEvaluation() {
        // Evaluate the results based on the data from the SPARQL storage
        TripleBlockStreamSupplier supplier = new FileBasedTripleBlockStreamSupplier(graphFiles,
                Stream.of(nodeMetadata).map(nm -> nm.getResourceUriTemplate()).toArray(String[]::new),
                Stream.of(nodeMetadata).map(nm -> nm.getAccessUriTemplate()).toArray(String[]::new),
                new GraphBasedTripleBlockStreamCreator(), new TTLTarGZBasedTripleBlockStreamCreator());
        // If only a subset should be used for the evaluation
        if(part2Evaluate < 1.0) {
            supplier = new SamplingTripleBlockStreamDecorator(supplier, part2Evaluate, seedGenerator.getNextSeed());
        }
        GraphValidator validator = SparqlBasedValidator.create(sparqlEndpoint);
        CrawledDataEvaluator evaluator = new SimpleCompleteEvaluator(supplier, validator);
        return evaluator.evaluate();
    }

    protected Model summarizeEvaluation(Map<Integer, EvaluationResult> results) throws Exception {
        EvaluationResult result = results.get(CrawledDataEvaluator.TOTAL_EVALUATION_RESULTS);
        // Write results into a Jena model and send it to the BC
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(experimentUri), RDF.type, HOBBIT.Experiment);
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(model.createLiteralStatement(experimentResource, LDCBench.triplesEvaluated, result.checkedTriples));
        model.add(model.createLiteralStatement(experimentResource, LDCBench.truePositives, result.truePositives));
        model.add(model.createLiteralStatement(experimentResource, LDCBench.microRecall, result.recall));
        // Add runtime if > 0
        long runtime = endTimeStamp - startTimeStamp;
        if (runtime > 0) {
            model.add(model.createLiteralStatement(experimentResource, LDCBench.runtime, runtime));
        }

        // Add results from all nodes
        double macroRecall = 0;
        int macroRecallDenominator = 0;
        for (Map.Entry<Integer, EvaluationResult> entry : results.entrySet()) {
            if (entry.getKey() != CrawledDataEvaluator.TOTAL_EVALUATION_RESULTS) {
                Resource nodeResource = model.createResource(experimentUri + "_Node_" + entry.getKey());
                double recall = entry.getValue().recall;
                // When node has no data at all, recall would be NaN
                if (!Double.isNaN(recall)) {
                    macroRecall += recall;
                    macroRecallDenominator++;
                }
                // model.add(model.createStatement(experimentResource,
                // model.createProperty(LDCBench.getURI(), "node"), nodeResource));
                model.add(model.createLiteralStatement(nodeResource, LDCBench.microRecall, recall));
            }
        }
        macroRecall /= macroRecallDenominator;
        model.add(model.createLiteralStatement(experimentResource, LDCBench.macroRecall, macroRecall));

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

        if ((resourcesTimerTask != null) && (resourcesTimerTask.getTimestamps().size() > 1)
                && (resourcesTimerTask.getValues().size() > 1)) {
            Resource dataset = ResourceUsageDiagrams.createDataset(model, getHobbitSessionId());
            List<Long> timestamps = resourcesTimerTask.getTimestamps();
            List<ResourceUsageInformation> values = resourcesTimerTask.getValues();
            double maxCpuUsage = 0;
            double sumDiskUsage = 0;
            double sumMemoryUsage = 0;
            for (int i = 0; i < timestamps.size(); ++i) {
                ResourceUsageInformation info = values.get(i);
                ResourceUsageDiagrams.addPoint(model, dataset, getHobbitSessionId(), i,
                        timestamps.get(i) - startTimeStamp, info.getCpuStats().getTotalUsage(),
                        info.getDiskStats().getFsSizeSum(), info.getMemoryStats().getUsageSum());
                maxCpuUsage = Math.max(maxCpuUsage, info.getCpuStats().getTotalUsage());
                sumDiskUsage += info.getDiskStats().getFsSizeSum();
                sumMemoryUsage += info.getMemoryStats().getUsageSum();
            }
            model.add(experimentResource, LDCBench.resourceUsageOverTime, dataset);
            if (maxCpuUsage != 0) {
                model.addLiteral(experimentResource, LDCBench.totalCpuUsage, maxCpuUsage);
            }
            if (sumDiskUsage != 0) {
                model.addLiteral(experimentResource, LDCBench.averageDiskUsage, sumDiskUsage / timestamps.size());
            }
            if (sumMemoryUsage != 0) {
                model.addLiteral(experimentResource, LDCBench.averageMemoryUsage, sumMemoryUsage / timestamps.size());
            }
        }

        return model;
    }
}
