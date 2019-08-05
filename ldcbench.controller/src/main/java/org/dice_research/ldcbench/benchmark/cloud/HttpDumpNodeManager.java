package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class HttpDumpNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.httpDumpNodeWeight;
    }

    @Override
    public int weightOfLinkFrom(Class<?> nodeManager) {
        return 1;
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
            "LDCBENCH_USE_DUMP_FILE=" + Boolean.TRUE.toString()
        };
        return env;
    }

    @Override
    public String getImageName() {
        return HTTPNODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "http";
    }
}
