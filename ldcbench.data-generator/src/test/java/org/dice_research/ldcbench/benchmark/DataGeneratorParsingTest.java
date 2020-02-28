package org.dice_research.ldcbench.benchmark;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class DataGeneratorParsingTest {

    @Test
    public void testArray() {
        String array[];
        String parsed[];

        array = new String[] { "a.org", "b.com", "c.c.cc" };
        parsed = DataGenerator.parseStringArray(Arrays.toString(array));
        Assert.assertArrayEquals(array, parsed);

    }

    @Test
    public void testEmptyArray() {
        String array[];
        String parsed[];

        array = new String[] {};
        parsed = DataGenerator.parseStringArray(Arrays.toString(array));
        Assert.assertArrayEquals(array, parsed);
    }
}
