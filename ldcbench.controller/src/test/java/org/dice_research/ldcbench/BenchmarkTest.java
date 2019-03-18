package org.dice_research.ldcbench;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.*;
import org.hobbit.sdk.docker.builders.hobbit.*;

import org.dice_research.ldcbench.nodes.http.simple.SimpleHttpServerComponent;
import org.dice_research.ldcbench.benchmark.*;
import org.dice_research.ldcbench.system.SystemAdapter;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.utils.ModelsHandler;
import org.hobbit.sdk.utils.MultiThreadedImageBuilder;
import org.hobbit.sdk.utils.commandreactions.CommandReactionsBuilder;
import org.hobbit.vocab.HOBBIT;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.hobbit.core.Constants.*;

import static org.hobbit.sdk.Constants.BENCHMARK_URI;
import static org.hobbit.sdk.Constants.GIT_USERNAME;
import static org.dice_research.ldcbench.Constants.*;

/**
 * @author Pavel Smirnov
 */

public class BenchmarkTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkTest.class);

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private AbstractDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;
    SimpleHttpNodeBuilder httpNodeBuilder;

    public void init(Boolean useCachedImage) throws Exception {

        benchmarkBuilder = new BenchmarkDockerBuilder(new ExampleDockersBuilder(BenchmarkController.class, BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImage));
        dataGeneratorBuilder = new DataGenDockerBuilder(new ExampleDockersBuilder(DataGenerator.class, DATAGEN_IMAGE_NAME).useCachedImage(useCachedImage).addFileOrFolder("data"));

        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImage));
        evalModuleBuilder = new EvalModuleDockerBuilder(new ExampleDockersBuilder(EvalModule.class, EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImage));

        // FIXME do not build httpNode image as part of this project
        httpNodeBuilder = new SimpleHttpNodeBuilder(new ExampleDockersBuilder(SimpleHttpServerComponent.class, HTTPNODE_IMAGE_NAME).useCachedImage(useCachedImage));

//        benchmarkBuilder = new BenchmarkDockerBuilder(new PullBasedDockersBuilder(BENCHMARK_IMAGE_NAME));
//        dataGeneratorBuilder = new DataGenDockerBuilder(new PullBasedDockersBuilder(DATAGEN_IMAGE_NAME));
//        evalModuleBuilder = new EvalModuleDockerBuilder(new PullBasedDockersBuilder(EVALMODULE_IMAGE_NAME));

    }

    @Test
    public void buildImages() throws Exception {

        init(false);

        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(8);
        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.addTask(evalModuleBuilder);
        builder.addTask(httpNodeBuilder);
        builder.build();

    }

    @Test
    public void checkHealth() throws Exception {
        checkHealth(false);
    }

    @Test
    @Ignore
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }

    //Flush a queue of a locally running platform
    @Test
    @Ignore
    public void flushQueue(){
        QueueClient queueClient = new QueueClient(GIT_USERNAME);
        queueClient.flushQueue();
    }

    //Submit benchmark to a queue of a locally running platform
    @Test
    @Ignore
    public void submitToQueue() throws Exception {
        QueueClient queueClient = new QueueClient(GIT_USERNAME);
        queueClient.submitToQueue(BENCHMARK_URI, SYSTEM_URI, createBenchmarkParameters());

    }

    private void checkHealth(Boolean dockerized) throws Exception {

        String[] benchmarkParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createBenchmarkParameters(), ModelsHandler.readModelFromFile("benchmark.ttl"))) };
        String [] systemParamsStr = new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+  RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createSystemParameters(), ModelsHandler.readModelFromFile("system.ttl"))) };


        Boolean useCachedImages = true;
        init(useCachedImages);

        rabbitMqDockerizer = RabbitMqDockerizer.builder().useCachedContainer().build();

        environmentVariables.set(DOCKERIZED_KEY, dockerized.toString());
        environmentVariables.set(RABBIT_MQ_HOST_NAME_KEY, dockerized ? "rabbit" : "localhost");
        environmentVariables.set(HOBBIT_SESSION_ID_KEY, "session_"+String.valueOf(new Date().getTime()));


        Component benchmarkController = new BenchmarkController();
        Component dataGen = new DataGenerator();
        Component systemAdapter = new SystemAdapter();
        Component evalModule = new EvalModule();
        Component httpNode = new SimpleHttpServerComponent();

        if(dockerized) {

            benchmarkController = benchmarkBuilder.build();
            dataGen = dataGeneratorBuilder.build();
            evalModule = evalModuleBuilder.build();
            systemAdapter = systemAdapterBuilder.build();
            httpNode = httpNodeBuilder.build();
        }

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor();

        rabbitMqDockerizer.run();


        //comment the .systemAdapter(systemAdapter) line below to use the code for running from python
        CommandReactionsBuilder commandReactionsBuilder = new CommandReactionsBuilder(componentsExecutor, commandQueueListener)
                        .benchmarkController(benchmarkController).benchmarkControllerImageName(BENCHMARK_IMAGE_NAME)
                        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                        .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                        .systemAdapter(systemAdapter).systemAdapterImageName(SYSTEM_IMAGE_NAME)
                        .customContainerImage(httpNode, HTTPNODE_IMAGE_NAME)
                        //.customContainerImage(systemAdapter, DUMMY_SYSTEM_IMAGE_NAME)
                ;

        commandQueueListener.setCommandReactions(
                commandReactionsBuilder.containerCommandsReaction(), //comment this if you want to run containers on a platform instance (if the platform is running)
                commandReactionsBuilder.benchmarkSignalsReaction()
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        // Start components without sending command to queue. Components will be executed by SDK, not the running platform (if it is running)
        String benchmarkContainerId = "benchmark";
        String systemContainerId = "system";

//        componentsExecutor.submit(benchmarkController, benchmarkContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ createBenchmarkParameters() });
//        componentsExecutor.submit(systemAdapter, systemContainerId, new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+ createSystemParameters() });

        //Alternative. Start components via command queue (will be executed by the platform (if running))
        benchmarkContainerId = commandQueueListener.createContainer(benchmarkBuilder.getImageName(), "benchmark", benchmarkParamsStr);
        systemContainerId = commandQueueListener.createContainer(systemAdapterBuilder.getImageName(), "system" , systemParamsStr);

        environmentVariables.set("BENCHMARK_CONTAINER_ID", benchmarkContainerId);
        environmentVariables.set("SYSTEM_CONTAINER_ID", systemContainerId);

        commandQueueListener.waitForTermination();

        rabbitMqDockerizer.stop();

        if (componentsExecutor.anyExceptions()) {
            LOGGER.error("Some components didn't execute cleanly");
            for (Throwable e : componentsExecutor.getExceptions()) {
                LOGGER.error("- {}", e.toString());
            }
            Assert.fail();
        }
    }

    public static Model createBenchmarkParameters() throws IOException {
        Model model = createDefaultModel();
        Resource experimentResource = model.createResource(org.hobbit.core.Constants.NEW_EXPERIMENT_URI);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(experimentResource, LDCBench.seed, "100");
        model.add(experimentResource, LDCBench.numberOfNodes, "3");
        model.add(experimentResource, LDCBench.triplesPerNode, "100");
        model.add(experimentResource, LDCBench.averageNodeDelay, "5000");
        model.add(experimentResource, LDCBench.averageNodeGraphDegree, "3");
        model.add(experimentResource, LDCBench.averageRdfGraphDegree, "5");
        return model;

    }

    public static Model createSystemParameters() throws IOException {
        Model model = createDefaultModel();
        Resource experimentResource = model.createResource(org.hobbit.core.Constants.NEW_EXPERIMENT_URI);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);
        model.add(experimentResource, model.createProperty(BENCHMARK_URI+"#systemParam123"),"100");
        return model;
    }
}
