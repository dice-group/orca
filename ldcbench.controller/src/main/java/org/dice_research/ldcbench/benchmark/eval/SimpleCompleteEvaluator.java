package org.dice_research.ldcbench.benchmark.eval;

public class SimpleCompleteEvaluator implements CrawledDataEvaluator {

    protected GraphSupplier supplier;
    protected GraphValidator validator;
    
    @Override
    public EvaluationResult evaluate() {
        ValidationResult result = new ValidationResult();
        for(int i = 0; i < supplier.getNumberOfGraphs(); ++i) {
            result.add(validator.validate(supplier, i));
        }
        return new EvaluationResult(result);
    }
    
}
