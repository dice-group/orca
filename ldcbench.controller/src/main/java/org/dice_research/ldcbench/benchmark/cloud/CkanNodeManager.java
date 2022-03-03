package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class CkanNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.ckanNodeWeight;
    }

    @Override
    public boolean canBeHub() {
        return true;
    }

    @Override
    public String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode, long numberOfGraphs) {
        String[] env = new String[]{
            DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + 0,
            DataGenerator.ENV_NUMBER_OF_NODES_KEY + "=" + 1,
            DataGenerator.ENV_NUMBER_OF_GRAPHS_KEY + "=" + 1,
        };
        return env;
    }

    @Override
    public String getNodeImageName() {
        return CKANNODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "CKAN";
    }
}
