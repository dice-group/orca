package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class DereferencingHttpNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.dereferencingHttpNodeWeight;
    }

    @Override
    public int weightOfLinkFrom(Class<?> nodeManager) {
        if (nodeManager == CkanNodeManager.class) {
            return 0;
        }

        return 1;
    }

    @Override
    public boolean canBeHub() {
        return false;
    }

    @Override
    public String[] getNodeEnvironment() {
        return new String[]{
            "LDCBENCH_USE_DUMP_FILE=" + Boolean.FALSE.toString()
        };
    };

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
        return HTTPNODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "HTTP deref.";
    }
}
