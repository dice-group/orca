package org.dice_research.ldcbench.benchmark.eval.timer;

/**
 * An interface of a class that is able to count the triples that have been
 * stored by the benchmarked crawler.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface TripleCounter {

    /**
     * Returns the number of triples that have been stored by the benchmarked
     * crawler until this point in time.
     * 
     * @return the number of triples that have been stored by the benchmarked
     * crawler until this point in time.
     */
    public long countTriples();
}
