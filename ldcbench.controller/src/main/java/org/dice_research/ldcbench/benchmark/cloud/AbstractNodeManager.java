package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.data.NodeMetadata;

public abstract class AbstractNodeManager {
    public abstract String getImageName();

    public NodeMetadata getMetadata() {
        return new NodeMetadata();
    }
}
