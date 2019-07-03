package org.dice_research.ldcbench.benchmark.eval;

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
     *            graph supplier containing the ground truth graph
     * @param graphId
     *            the ID of the graph that should be validated
     * @return the result of the validation process
     */
    public ValidationResult validate(GraphSupplier supplier, int graphId);
}
