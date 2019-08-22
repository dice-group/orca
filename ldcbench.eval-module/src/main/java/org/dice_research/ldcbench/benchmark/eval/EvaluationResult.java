package org.dice_research.ldcbench.benchmark.eval;

public class EvaluationResult extends ValidationResult {

    public final double recall;

    public EvaluationResult(ValidationResult v) {
        super(v.checkedTriples, v.truePositives);
        this.recall = v.truePositives / (double) v.checkedTriples;
    }

}
