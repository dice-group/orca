package org.dice_research.ldcbench.generate;

import java.util.function.LongUnaryOperator;

import org.dice_research.ldcbench.graph.GraphBuilder;

/**
 * A class that is able to generate a sequence of independent random seeds
 * given a single seed.
 * FIXME: This implementation is probably not the right way to do it.
 *
 */
public class SeedGenerator implements LongUnaryOperator {
    private long seed;
    private int quadCoef = 31;

    /**
     * Constructor.
     *
     * @param seed
     *            the single original seed
     */
    public SeedGenerator(long seed) {
        this.seed = seed;
    }

    /**
     * Generates a seed from the sequence given the seed index.
     * {@link GraphBuilder}.
     *
     * @param i
     *            the seed index
     * @return seed
     *            the i-th seed in the sequence.
     */
     public long applyAsLong(long i) {
         return seed + quadCoef * (i + 1) * (i + 1);
     }
}
