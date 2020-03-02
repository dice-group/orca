package org.dice_research.ldcbench.rdfa.gen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class RDFaEntranceFileGeneratorTest {

    @Test
    public void test() throws IOException {
        Set<String> urls = new HashSet<>(Arrays.asList("http://example.org/resource1", "http://example.org/resource2","http://example2.org/externalLink"));
        
        File htmlFile = File.createTempFile("rdfa-test", ".html");
        htmlFile.deleteOnExit();
        
        RDFaEntranceFileGenerator generator = new RDFaEntranceFileGenerator();
        generator.generate(htmlFile, "http://example.org/resource0", urls);
        
        String expectedFile = null;
        try(InputStream in = this.getClass().getResourceAsStream("/expectedRDFaEntranceFile.html")) {
            Assert.assertNotNull("Couldn't load expected content from resources.", in);
            expectedFile = IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        String createdFile = FileUtils.readFileToString(htmlFile, StandardCharsets.UTF_8);
        
        Assert.assertEquals(expectedFile, createdFile);
    }
}
