package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class SparqlNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.sparqlNodeWeight;
    }

    @Override
    public int weightOfLinkFrom(Class<?> nodeManager) {
        if (nodeManager == CkanNodeManager.class) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean canBeHub() {
        return false;
    }

    @Override
    public String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode) {
        String[] env = new String[]{
            DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + averageRdfGraphDegree,
            DataGenerator.ENV_NUMBER_OF_EDGES_KEY + "=" + triplesPerNode,
        };
        return env;
    }

    @Override
    public String getImageName() {
        return SPARQLNODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "sparql";
    }
}
