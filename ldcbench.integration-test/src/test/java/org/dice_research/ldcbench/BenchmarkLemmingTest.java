package org.dice_research.ldcbench;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.dice_research.ldcbench.benchmark.FileBasedRDFGraphGenerator;
import org.dice_research.ldcbench.benchmark.LemmingBasedBenchmarkController;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.utils.ModelsHandler;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class BenchmarkLemmingTest extends BenchmarkTestBase {

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Override
    public void init(Boolean useCachedImage) throws Exception {
        benchmarkImage = LemmingBasedBenchmarkController.LEMMING_DOCKER_IMAGE;
        benchmarkClass = LemmingBasedBenchmarkController.class;

        dataGeneratorImage = LemmingBasedBenchmarkController.LEMMING_DOCKER_IMAGE;
        dataGeneratorClass = FileBasedRDFGraphGenerator.class;

        super.init(useCachedImage);
    }

    @Test
    public void executeBenchmark() throws Exception {
        executeBenchmark(false);
    }

    // Flush a queue of a locally running platform
    // @Test
    // @Ignore
    public void flushQueue() {
        QueueClient queueClient = new QueueClient(org.hobbit.sdk.Constants.GIT_USERNAME);
        queueClient.flushQueue();
    }

    // Submit benchmark to a queue of a locally running platform
    // @Test
    // @Ignore
    public void submitToQueue() throws Exception {
        QueueClient queueClient = new QueueClient(org.hobbit.sdk.Constants.GIT_USERNAME);
        queueClient.submitToQueue("", org.dice_research.ldcbench.Constants.SYSTEM_URI, createBenchmarkParameters());
    }

    public Model createBenchmarkParameters() throws IOException {
        return ModelsHandler.readModelFromFile("test-benchmark-parameters-lemming.ttl");
    }

    @Override
    protected Component getController(boolean dockerized) {
        return getComponent(dockerized, LemmingBasedBenchmarkController.class, benchmarkBuilder);
    }

    @Override
    protected Component getDataGen(boolean dockerized) {
        return getComponent(dockerized, FileBasedRDFGraphGenerator.class, dataGeneratorBuilder);
    }
}
