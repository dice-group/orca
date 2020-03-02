package org.dice_research.ldcbench.utils.tar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * A simple {@link FileHandler} which extracts all files to a given directory
 * (including the subdirectories defined in the archive).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SimpleWritingFileHandler implements FileHandler {

    /**
     * The directory to which all files will be extracted.
     */
    protected String rootDir;

    /**
     * Constructor.
     * 
     * @param rootDir the directory to which all files will be extracted
     */
    public SimpleWritingFileHandler(String rootDir) {
        if (!rootDir.endsWith(File.separator)) {
            rootDir += File.separator;
        }
        this.rootDir = rootDir;
    }

    @Override
    public void handleStream(InputStream in, String name, long size) throws IOException {
        File outputFile = new File(rootDir + name);
        File parent = outputFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            IOUtils.copyLarge(in, out, 0, size);
        }
    }

}
