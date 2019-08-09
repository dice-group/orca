package org.dice_research.ldcbench.benchmark.node;

/**
 * A simple node size determiner that is assigning the same given size to all
 * nodes.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class StaticNodeSizeDeterminer implements NodeSizeDeterminer {

    protected int averageNodeSize;

    public StaticNodeSizeDeterminer(int averageNodeSize) {
        this.averageNodeSize = averageNodeSize;
    }

    @Override
    public int getNodeSize() {
        return averageNodeSize;
    }
}
