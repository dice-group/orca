package org.dice_research.ldcbench.generate;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SequentialSeedGeneratorTest {

    /**
     * Test which makes sure that multiple sequential seed generators are generating
     * different seeds.
     */
    @Test
    public void testDiff() {
        long seed = 123;
        SeedGenerator generators[] = new SeedGenerator[100];
        for (int i = 0; i < generators.length; ++i) {
            generators[i] = new SequentialSeedGenerator(seed, i, generators.length);
        }
        Set<Long> seeds = new HashSet<Long>(200 * generators.length);
        for (int i = 0; i < generators.length; ++i) {
            for (int j = 0; j < 100; ++j) {
                seed = generators[i].getNextSeed();
                Assert.assertFalse("seed " + seed + " of generator #" + i + " is already known.", seeds.contains(seed));
                seeds.add(seed);
            }
        }
    }

    /**
     * Makes sure that two seed generators are creating the same seed sequence if
     * they are initialized in the same way.
     */
    @Test
    public void testSame() {
        SeedGenerator generator1 = new SequentialSeedGenerator(123);
        SeedGenerator generator2 = new SequentialSeedGenerator(123);
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals(generator1.getNextSeed(), generator2.getNextSeed());
        }
    }
}
