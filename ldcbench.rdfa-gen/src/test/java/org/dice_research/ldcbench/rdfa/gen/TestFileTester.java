package org.dice_research.ldcbench.rdfa.gen;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test ensures that the test files can be loaded.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class TestFileTester {

    @Test
    public void testOriginalFiles() {
        Map<String, String> tests = RDFaDataGenerator.loadTestFiles();
        Assert.assertFalse("Couldn't load any test files.", tests.isEmpty());
        System.out.println("Loaded " + tests.size() + " files.");

        SortedMap<String, File> ttlFiles = new TreeMap<>();
        RDFaDataGenerator.convertTestUrisToFiles(tests.values(), ttlFiles);

        Model model;
        for (File f : ttlFiles.values()) {
            model = ModelFactory.createDefaultModel();
            try {
                RDFDataMgr.read(model, f.toURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            Assert.assertFalse("The model read from \"" + f.toString() + "\" is empty.", model.isEmpty());
        }
    }

    /**
     * A copy of {@link RDFaReplacementTest#testUriReplacementInFiles()} that
     * repeats the test on all ttl files and checks whether they can be loaded after
     * changing them.
     * 
     * @throws IOException
     */
    @Test
    public void testUriReplacementInFiles() throws IOException {
        Map<String, String> tests = RDFaDataGenerator.loadTestFiles();
        Assert.assertFalse("Couldn't load any test files.", tests.isEmpty());
        System.out.println("Loaded " + tests.size() + " files.");

        SortedMap<String, File> ttlFiles = new TreeMap<>();
        RDFaDataGenerator.convertTestUrisToFiles(tests.values(), ttlFiles);

        // Copy the files for our test
        RDFaReplacementTest.copyFilesToTempDir("replacement-test-ttl", ttlFiles);

        // Load the tests and replace URIs
        RDFaDataGenerator.replaceUrisInFiles(ttlFiles.values(), RDFaDataGenerator.RDFA_TEST_DOMAIN,
                RDFaReplacementTest.NODE_DOMAIN);

        // Go through the files and try to parse them
        Model model;
        for (File f : ttlFiles.values()) {
            model = ModelFactory.createDefaultModel();
            try {
                RDFDataMgr.read(model, f.toURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            Assert.assertFalse("The model read from \"" + f.toString() + "\" is empty.", model.isEmpty());
        }
    }

}
