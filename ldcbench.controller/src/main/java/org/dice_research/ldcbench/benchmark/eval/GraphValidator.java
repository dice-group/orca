package org.dice_research.ldcbench.benchmark.eval;

public interface GraphValidator {

    public ValidationResult validate(GraphSupplier supplier, int graphId);
}
