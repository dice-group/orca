package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
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

    @Override
    public void init() throws Exception {
        super.init();

        // Get the experiment URI
        experimentUri = EnvVariables.getString(Constants.HOBBIT_EXPERIMENT_URI_KEY, LOGGER);

        // TODO Initialize the receiving of graph data

        LOGGER.info("Evaluation module initialized.");
    }

    @Override
    public void run() throws Exception {
        // Let the BC now that this module is ready
        sendToCmdQueue(Commands.EVAL_MODULE_READY_SIGNAL);

        // TODO wait for all the graphs to be sent

        // TODO wait for the crawling to finish

        // TODO evaluate the results based on the data from the SPARQL storage
        // Create result model and terminate
        Model model = summarizeEvaluation();
        LOGGER.info("The result model has " + model.size() + " triples.");
        sendResultModel(model);
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close(){
        // Free the resources you requested here
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

    protected Model summarizeEvaluation() throws Exception {
        // All tasks/responsens have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(experimentUri), RDF.type, HOBBIT.Experiment);
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);

        return model;
    }
}
