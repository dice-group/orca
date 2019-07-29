package org.dice_research.ldcbench.benchmark.eval;

import java.util.Map;

/**
 * Interface of an evaluator for crawled data.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface CrawledDataEvaluator {
    /**
     * Key for the evaluation map corresponding to the global evaluation results.
     */
    public static final int TOTAL_EVALUATION_RESULTS = -1;

    /**
     * Evaluates the crawled data based on the arguments it got during its creation.
     *
     * @return list of results of the evaluation
     *         first element is global statistics,
     *         other elements are per-graph
     */
    public Map<Integer, EvaluationResult> evaluate();
}
