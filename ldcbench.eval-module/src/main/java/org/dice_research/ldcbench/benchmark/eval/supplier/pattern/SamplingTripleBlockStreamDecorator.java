package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 * A simple decorator for a triple block stream that reduces the stream to an
 * amount that is close to but in most cases still larger than the given
 * acceptance rate. Note that the decorator will always forward the first
 * element of the stream to ensure that a stream is never empty (unless the
 * original supplier created an empty stream).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
@NotThreadSafe
public class SamplingTripleBlockStreamDecorator implements TripleBlockStreamSupplierDecorator {

    /**
     * The decorated supplier instance.
     */
    protected TripleBlockStreamSupplier decorated;
    /**
     * The given acceptance rate.
     */
    protected double acceptanceRate;
    /**
     * The random number generator used to sample whether a triple block should be
     * forwarded.
     */
    protected Random random;
    /**
     * An internal flag used to decide whether the given block is the first of a
     * decorated stream.
     */
    protected AtomicBoolean isFirstBlock;

    /**
     * Constructor.
     * 
     * @param decorated      The decorated supplier instance
     * @param acceptanceRate The given acceptance rate (between 0.0 and 1.0)
     * @param seed           A seed used to initialize the internal random number
     *                       generator that is used to sample whether a triple block
     *                       should be forwarded.
     */
    public SamplingTripleBlockStreamDecorator(TripleBlockStreamSupplier decorated, double acceptanceRate, long seed) {
        super();
        this.decorated = decorated;
        this.acceptanceRate = acceptanceRate;
        this.random = new Random(seed);
    }

    @Override
    public Stream<ElementTriplesBlock> getTripleBlocks(int graphId) {
        Stream<ElementTriplesBlock> stream = decorated.getTripleBlocks(graphId);
        stream = stream.filter(new DecoratedTripleBlockStreamFilter(acceptanceRate, random));
        return stream;
    }

    @Override
    public int getNumberOfGraphs() {
        return decorated.getNumberOfGraphs();
    }

    @Override
    public TripleBlockStreamSupplier getDecorated() {
        return decorated;
    }

    /**
     * A simple predicate that returns true if the given block is either the first
     * block in the stream or if a randomly chosen number is smaller than the given
     * acceptance rate.
     * 
     * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
     *
     */
    public static class DecoratedTripleBlockStreamFilter implements Predicate<ElementTriplesBlock> {
        /**
         * The random number generator used to sample whether a triple block should be
         * forwarded.
         */
        protected Random random;
        /**
         * The given acceptance rate.
         */
        protected double acceptanceRate;
        /**
         * An internal flag used to decide whether the given block is the first of a
         * decorated stream.
         */
        protected AtomicBoolean isFirstBlock = new AtomicBoolean(true);

        /**
         * Constructor.
         * 
         * @param random The random number generator used to sample whether a triple
         *               block should be forwarded.
         */
        public DecoratedTripleBlockStreamFilter(double acceptanceRate, Random random) {
            this.random = random;
        }

        @Override
        public boolean test(ElementTriplesBlock t) {
            return isFirstBlock.getAndSet(false) || random.nextDouble() < acceptanceRate;
        }
    }

}
