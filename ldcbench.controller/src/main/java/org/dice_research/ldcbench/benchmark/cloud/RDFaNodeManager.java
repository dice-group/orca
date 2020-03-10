package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.benchmark.DataGenerator;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.dice_research.ldcbench.Constants;

/**
 * Manager class for an RDFa node. 
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFaNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.rdfaNodeWeight;
    }

    @Override
    public boolean canBeHub() {
        return false;
    }

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
        return Constants.RDFANODE_IMAGE_NAME;
    }

    @Override
    public String getLabel() {
        return "RDFa";
    }
    
    @Override
    public String getDataGeneratorImageName() {
        return Constants.RDFADATAGEN_IMAGE_NAME;
    }
}
