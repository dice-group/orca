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
    public boolean canBeHub() {
        return false;
    }

    @Override
    public String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode, long numberOfGraphs) {
        String[] env = new String[]{
            DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + averageRdfGraphDegree,
            DataGenerator.ENV_NUMBER_OF_EDGES_KEY + "=" + triplesPerNode,
            DataGenerator.ENV_NUMBER_OF_GRAPHS_KEY + "=" + 1,
        };
        return env;
    }

    @Override
    public String getNodeImageName() {
        return SPARQLNODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "SPARQL";
    }
}
