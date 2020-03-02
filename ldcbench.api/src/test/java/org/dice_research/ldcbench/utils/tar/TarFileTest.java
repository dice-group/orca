package org.dice_research.ldcbench.utils.tar;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

public class TarFileTest {

    @Test
    public void testWithGZ() throws IOException {
        runTest(true);
    }

    @Test
    public void testWithoutGZ() throws IOException {
        runTest(false);
    }

    public void runTest(boolean useGZip) throws IOException {
        // Generate test data
        Map<String, byte[]> originalContents = generateTestData();
        // Write test data to (temporary) files
        SortedMap<String, File> originalFiles = writeToFiles(originalContents);

        TarFileGenerator generator = new TarFileGenerator();
        File tarFile = File.createTempFile("tar-file", useGZip ? ".tar.gz" : ".tar");
        generator.generateTarFile(tarFile, originalFiles, useGZip);

        File rootDir = File.createTempFile("tar-dir", "");
        if (rootDir.exists()) {
            rootDir.delete();
        }
        Assert.assertTrue("Couldn't create temp directory.", rootDir.mkdir());
        rootDir.deleteOnExit();

        SimpleWritingFileHandler handler = new SimpleWritingFileHandler(rootDir.getAbsolutePath());
        TarFileReader reader = new TarFileReader();
        Set<String> readFiles = new TreeSet<>(reader.read(tarFile, handler, useGZip));
        // Check file names
        Assert.assertArrayEquals(originalFiles.keySet().toArray(), readFiles.toArray());

        // Check file contents
        int prefixLength = rootDir.getAbsolutePath().length() + 1;
        int checkedFiles = checkFilesRecursively(rootDir, prefixLength, originalContents);

        Assert.assertEquals("Number of files in the created directory differs from the expected number of Files.",
                originalContents.size(), checkedFiles);
    }

    private Map<String, byte[]> generateTestData() {
        Map<String, byte[]> originalContents = new HashMap<>();

        // generate some files
        originalContents.put("README.md", "README! Or not :P".getBytes(StandardCharsets.UTF_8));
        originalContents.put("dir1/file01", "Hello tar file.".getBytes(StandardCharsets.UTF_8));

        // Generate a larger file
        int numbers = 5000;
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * numbers);
        for (int i = 0; i < numbers; ++i) {
            buffer.putInt(i);
        }
        originalContents.put("dir1/a/b/c/content.dat", buffer.array());

        return originalContents;
    }

    private SortedMap<String, File> writeToFiles(Map<String, byte[]> originalContents) throws IOException {
        SortedMap<String, File> originalFiles = new TreeMap<>();
        File file;
        for (String name : originalContents.keySet()) {
            file = File.createTempFile("test-file", ".tmp");
            file.deleteOnExit();
            FileUtils.writeByteArrayToFile(file, originalContents.get(name));
            originalFiles.put(name, file);
        }
        return originalFiles;
    }

    private int checkFilesRecursively(File rootDir, int prefixLength, Map<String, byte[]> originalContents)
            throws ArrayComparisonFailure, IOException {
        int fileCount = 0;
        for (File file : rootDir.listFiles()) {
            if (file.isDirectory()) {
                fileCount += checkFilesRecursively(file, prefixLength, originalContents);
            } else {
                String name = file.getAbsolutePath().substring(prefixLength);
                Assert.assertTrue("Couldn't find \"" + name + "\" within the list of expected files.",
                        originalContents.containsKey(name));
                Assert.assertArrayEquals("File content of \"" + name + "\" differs from the expected content.",
                        originalContents.get(name), FileUtils.readFileToByteArray(file));
                ++fileCount;
            }
        }
        return fileCount;
    }

}
