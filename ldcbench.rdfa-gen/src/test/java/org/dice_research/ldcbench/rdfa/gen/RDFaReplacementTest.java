package org.dice_research.ldcbench.rdfa.gen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import junit.framework.Assert;

public class RDFaReplacementTest {

    protected static final String NODE_DOMAIN = "http://example.org/";

    private static final String EXPECTED_HTML_FILES = "/expectedHTMLFiles/";
    private static final String EXPECTED_TTL_FILES = "/expectedTTLFiles/";

    @Test
    public void testUriReplacementInFiles() throws IOException {
        Map<String, String> tests;
        try (InputStream is = this.getClass().getResourceAsStream("/test-manifest.ttl")) {
            Assert.assertNotNull("Couldn't load manifest from resources.", is);

            ManifestProcessor processor = new ManifestProcessor();
            tests = processor.loadTests(is);
        }

        SortedMap<String, File> htmlFiles = new TreeMap<>();
        SortedMap<String, File> ttlFiles = new TreeMap<>();
        RDFaDataGenerator.convertTestUrisToFiles(tests.keySet(), htmlFiles);
        RDFaDataGenerator.convertTestUrisToFiles(tests.values(), ttlFiles);

        // Copy the files for our test
        copyFilesToTempDir("replacement-test-html", htmlFiles);
        copyFilesToTempDir("replacement-test-ttl",ttlFiles);

        // Load the tests and replace URIs
        RDFaDataGenerator.replaceUrisInFiles(htmlFiles.values(), RDFaDataGenerator.RDFA_TEST_DOMAIN, NODE_DOMAIN);
        RDFaDataGenerator.replaceUrisInFiles(ttlFiles.values(), RDFaDataGenerator.RDFA_TEST_DOMAIN, NODE_DOMAIN);

        compare(EXPECTED_HTML_FILES, htmlFiles.values());
        compare(EXPECTED_TTL_FILES, ttlFiles.values());
    }

    public static void copyFilesToTempDir(String prefix, SortedMap<String, File> files) throws IOException {
        File tempDir = File.createTempFile(prefix, "");
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        // Go through the list of files and add all the files to the temp directory
        File original;
        File copy;
        for (Entry<String, File> e : files.entrySet()) {
            original = e.getValue();
            copy = new File(tempDir, original.getName());
            FileUtils.copyFile(original, copy);
            e.setValue(copy);
            copy.deleteOnExit();
        }
    }

    public void compare(String expectedDir, Collection<File> files) throws IOException {
        String expected;
        String processed;
        for (File f : files) {
            processed = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            try (InputStream is = this.getClass().getResourceAsStream(expectedDir + f.getName())) {
                Assert.assertNotNull(is);
                expected = IOUtils.toString(is, StandardCharsets.UTF_8);
            }
            Assert.assertEquals("File " + f.getName() + " does not have the expected content.", expected, processed);
        }
    }
}
