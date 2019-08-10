package org.dice_research.ldcbench.benchmark.node;

/**
 * An interface defining the methods necessary to get the node sizes (in number of triples).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface NodeSizeDeterminer {

    /**
     * Returns the size of the next node in the synthetic cloud.
     * 
     * @return a node size
     */
    public int getNodeSize();
}
