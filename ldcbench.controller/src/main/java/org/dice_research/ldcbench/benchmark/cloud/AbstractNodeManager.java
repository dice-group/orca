package org.dice_research.ldcbench.benchmark.cloud;

import org.dice_research.ldcbench.Constants;
import org.dice_research.ldcbench.data.NodeMetadata;

public abstract class AbstractNodeManager implements NodeManager {
    
    public int weightOfLinkFrom(Class<?> nodeManager) {
        return 1;
    };

    public String[] getNodeEnvironment() {
        return new String[]{};
    };

    public NodeMetadata getMetadata() {
        return new NodeMetadata();
    }
    
    @Override
    public String getDataGeneratorImageName() {
        return Constants.DATAGEN_IMAGE_NAME;
    }
}
