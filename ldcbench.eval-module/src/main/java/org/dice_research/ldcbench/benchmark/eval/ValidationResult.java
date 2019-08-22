package org.dice_research.ldcbench.benchmark.eval;

public class ValidationResult {

    public long checkedTriples;
    public long truePositives;

    public ValidationResult() {
        this(0, 0);
    }

    public ValidationResult(long checkedTriples, long truePositives) {
        this.checkedTriples = checkedTriples;
        this.truePositives = truePositives;
    }

    public void add(ValidationResult v2) {
        this.checkedTriples += v2.checkedTriples;
        this.truePositives += v2.truePositives;
    }
}
