package org.dice_research.ldcbench.benchmark.cloud;

import org.dice_research.ldcbench.data.NodeMetadata;

public abstract class AbstractNodeManager {
    public abstract int weightOfLinkFrom(Class<?> nodeManager);

    public abstract boolean canBeHub();

    public abstract String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode);

    public abstract String getImageName();

    public abstract String getLabel();

    public NodeMetadata getMetadata() {
        return new NodeMetadata();
    }
}
