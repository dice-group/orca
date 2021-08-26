package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

/**
 * The interface of a class that decorates a {@link TripleBlockStreamSupplier}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface TripleBlockStreamSupplierDecorator extends TripleBlockStreamSupplier {

    /**
     * Getter for the decorated {@link TripleBlockStreamSupplier} instance.
     * 
     * @return the decorated instance
     */
    public TripleBlockStreamSupplier getDecorated();
}
