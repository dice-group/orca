package org.dice_research.ldcbench.benchmark.eval;

/**
 * A simple implementation of the {@link CrawledDataEvaluator} inteface which
 * iterates over all triples of all graphs and checks whether they can be found
 * in the crawled data.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SimpleCompleteEvaluator implements CrawledDataEvaluator {

    /**
     * Supplier used to access the generated graphs, i.e., the ground truth for the
     * evaluation.
     */
    protected GraphSupplier supplier;
    /**
     * The validator used to validate the single graphs.
     */
    protected GraphValidator validator;

    /**
     * Constructor.
     * 
     * @param supplier
     *            Supplier used to access the generated graphs, i.e., the ground
     *            truth for the evaluation.
     * @param validator
     *            The validator used to validate the single graphs.
     */
    public SimpleCompleteEvaluator(GraphSupplier supplier, GraphValidator validator) {
        this.supplier = supplier;
        this.validator = validator;
    }

    @Override
    public EvaluationResult evaluate() {
        ValidationResult result = new ValidationResult();
        for (int i = 0; i < supplier.getNumberOfGraphs(); ++i) {
            result.add(validator.validate(supplier, i));
        }
        return new EvaluationResult(result);
    }

}
