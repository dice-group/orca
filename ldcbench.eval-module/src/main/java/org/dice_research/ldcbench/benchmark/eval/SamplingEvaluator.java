package org.dice_research.ldcbench.benchmark.eval;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplingEvaluator implements CrawledDataEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEvaluator.class);

    protected long seed;

    @Override
    public Map<Integer, EvaluationResult> evaluate() {
        Random random = new Random(seed);
        // TODO
        return null;
    }
}
