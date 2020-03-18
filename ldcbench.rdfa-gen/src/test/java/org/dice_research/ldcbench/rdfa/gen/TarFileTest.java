package org.dice_research.ldcbench.rdfa.gen;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.dice_research.ldcbench.utils.tar.TarFileGenerator;
import org.dice_research.ldcbench.utils.tar.TarGZBasedTTLModelIterator;
import org.junit.Assert;
import org.junit.Test;

/**
 * A test that ensures that the tests can be put into a tar file and read again.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class TarFileTest {

    @Test
    public void testTarFile() throws IOException {
        Map<String, String> tests = RDFaDataGenerator.loadTestFiles();
        Assert.assertFalse("Couldn't load any test files.", tests.isEmpty());
        System.out.println("Loaded " + tests.size() + " files.");

        SortedMap<String, File> ttlFiles = new TreeMap<>();
        RDFaDataGenerator.convertTestUrisToFiles(tests.values(), ttlFiles);
        ttlFiles = RDFaDataGenerator.replaceUrisInMapping(ttlFiles, RDFaDataGenerator.RDFA_TEST_DOMAIN, "");

        File tarFile = File.createTempFile("test", ".tar.gz");
        tarFile.deleteOnExit();
        // Create file
        TarFileGenerator generator = new TarFileGenerator();
        generator.generateTarFile(tarFile, ttlFiles, true);
        // Try to iterate over the models
        int count = 0;
        TarGZBasedTTLModelIterator iterator = TarGZBasedTTLModelIterator.create(tarFile);
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        Assert.assertEquals("Not all tests have been found", tests.size(), count);
    }

}
