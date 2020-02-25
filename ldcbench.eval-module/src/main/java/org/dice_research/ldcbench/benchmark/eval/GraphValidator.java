package org.dice_research.ldcbench.benchmark.eval;

import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.TripleBlockStreamSupplier;

/**
 * Interface for a class which can validate a crawled graph based on the given
 * graph information.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface GraphValidator {

    /**
     * Validates the graph with the given ID by requesting the ground truth from the
     * given supplier.
     * 
     * @param supplier
     *            supplier containing the ground truth graphs
     * @param graphId
     *            the ID of the graph that should be validated
     * @return the result of the validation process
     */
    public ValidationResult validate(TripleBlockStreamSupplier supplier, int graphId);
}
