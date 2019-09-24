package org.dice_research.ldcbench.generate;

import java.util.function.LongSupplier;

/**
 * This interface takes generates seeds for initializing random number generators.
 */
public interface SeedGenerator extends LongSupplier {

    /**
     * Returns the next seed of this generator.
     * 
     * @return the next seed generated
     */
    public long getNextSeed();
    
    @Override
    default long getAsLong() {
        return getNextSeed();
    }
}
