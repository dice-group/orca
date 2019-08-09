package org.dice_research.ldcbench.benchmark.node;

import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

/**
 * This class will determine the node size based on a power law distribution
 * which is initialized with the given average node size. Note that the lowest
 * number it returns is 1.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class ExponentialDistBasedNodeSizeDeterminer implements NodeSizeDeterminer {

    protected ExponentialDistribution distribution;

    /**
     * Constructor.
     * 
     * @param averageNodeSize
     *            the average node size
     * @param seed
     *            the seed used to initialize the internal random number generator
     *            used for sampling the numbers
     */
    public ExponentialDistBasedNodeSizeDeterminer(int averageNodeSize, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.createRandomGenerator(new Random(seed));
        distribution = new ExponentialDistribution(rng, averageNodeSize - 1);
    }

    @Override
    public int getNodeSize() {
        return (int) Math.round(distribution.sample()) + 1;
    }
}
