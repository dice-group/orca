package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.dice_research.ldcbench.Constants;

/**
 * Manager class for a microformat node.
 *
 * @author Thoren Grüttemeier (thoreng@uni-paderborn.de)
 *
 */
public class MicroformatNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.microformatNodeWeight;
    }

    @Override
    public boolean canBeHub() {
        return false;
    }

    @Override
    public String[] getNodeEnvironment() {
        return new String[]{
            "LDCBENCH_USE_SINGLE_FILE=" + Boolean.FALSE.toString()
        };
    };

    @Override
    public String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode) {
        String[] env = new String[]{
            DataGenerator.ENV_AVERAGE_DEGREE_KEY + "=" + 0,
            DataGenerator.ENV_NUMBER_OF_NODES_KEY + "=" + 1,
        };
        return env;
    }

    @Override
    public String getNodeImageName() {
        return Constants.HENODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "Microformat";
    }

    @Override
    public String getDataGeneratorImageName() {
        return Constants.MICROFORMATGEN_IMAGE_NAME;
    }
}
