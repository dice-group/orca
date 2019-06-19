package org.dice_research.ldcbench;

import org.hobbit.sdk.utils.MultiThreadedImageBuilder;
import org.junit.Test;

/**
 * @author Pavel Smirnov
 */

public class ImageBuilder extends BenchmarkTestBase {
    @Test
    public void buildImages() throws Exception {
        init(false);

        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(8);
        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.addTask(evalModuleBuilder);
        builder.build();
    }
}
