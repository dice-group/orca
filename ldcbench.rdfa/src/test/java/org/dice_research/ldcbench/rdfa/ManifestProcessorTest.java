package org.dice_research.ldcbench.rdfa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;

public class ManifestProcessorTest {

    private static final String EXPECTED_TESTS[][] = new String[][] {
            { "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0001.html",
                    "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0001.ttl" },
            { "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0006.html",
                    "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0006.ttl" },
            { "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0007.html",
                    "http://rdfa.info/test-suite/test-cases/rdfa1.0/html4/0007.ttl" } };

    @Test
    public void test() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/test-manifest.ttl")) {
            Assert.assertNotNull("Couldn't load manifest from resources.", is);

            ManifestProcessor processor = new ManifestProcessor();
            Map<String, String> tests = processor.loadTests(is);

            for (int i = 0; i < EXPECTED_TESTS.length; ++i) {
                Assert.assertTrue("Couldn't find " + EXPECTED_TESTS[i][0] + " within the list of tests.",
                        tests.containsKey(EXPECTED_TESTS[i][0]));
                Assert.assertEquals(EXPECTED_TESTS[i][1], tests.get(EXPECTED_TESTS[i][0]));
            }
            Assert.assertEquals(tests.size(), EXPECTED_TESTS.length);
        }
    }
}
