package org.dice_research.ldcbench.benchmark.eval;

/**
 * Interface of an evaluator for crawled data.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface CrawledDataEvaluator {

    /**
     * Evaluates the crawled data based on the arguments it got during its creation.
     *
     * @return the result of the evaluation
     */
    public EvaluationResult evaluate();
}
