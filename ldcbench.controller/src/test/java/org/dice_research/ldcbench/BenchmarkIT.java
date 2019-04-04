package org.dice_research.ldcbench;

import org.junit.Test;

/**
 * @author Pavel Smirnov
 */

public class BenchmarkIT extends BenchmarkTestBase {
    @Test
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }
}
