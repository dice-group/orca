package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.vocab.LDCBench;
import static org.dice_research.ldcbench.Constants.*;

public class DereferencingHttpNodeManager extends AbstractNodeManager {
    public static Property getBenchmarkParameter() {
        return LDCBench.dereferencingHttpNodeWeight;
    }

    @Override
    public String getImageName() {
        return HTTPNODE_IMAGE_NAME;
    }
}
