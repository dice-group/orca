package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class CkanNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.ckanNodeWeight;
    }

    @Override
    public String getImageName() {
        return CKANNODE_IMAGE_NAME;
    }
}
