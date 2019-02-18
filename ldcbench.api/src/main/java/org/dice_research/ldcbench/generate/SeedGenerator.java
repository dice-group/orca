package org.dice_research.ldcbench.generate;

import java.util.function.IntUnaryOperator;

/**
 * A class that is able to generate a sequence of independent random seeds
 * given a single seed.
 * FIXME: This implementation is probably not the right way to do it.
 *
 */
public class SeedGenerator implements IntUnaryOperator {
    private int seed;
    private int quadCoef = 31;

    /**
     * Constructor.
     *
     * @param seed
     *            the single original seed
     */
    public SeedGenerator(int seed) {
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
     public int applyAsInt(int i) {
         return seed + quadCoef * (i + 1) * (i + 1);
     }
}
