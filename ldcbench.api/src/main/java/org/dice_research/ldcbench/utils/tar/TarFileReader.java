package org.dice_research.ldcbench.utils.tar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * A simple tar file reader taking the given file and forwarding the single
 * entries to a {@link FileHandler}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class TarFileReader {

    /**
     * Reads the given file as tar file and forwards the single streams for the
     * files within the tar file to the given {@link FileHandler}. The given flag
     * shows whether the tar file is has been further compressed using gzip. Returns
     * a set of the files that have been found within the tar file and that hvae
     * been forwarded to the {@link FileHandler}.
     * 
     * @param file    the tar file that should be read
     * @param handler the handler that is used to process the single files
     * @param gzipped if the flag is {@code true} the file is decompressed before
     *                processing
     * @return a set of the files that have been found within the tar file
     * @throws IOException if an IO error occurs while reading the tar file
     */
    public Set<String> read(File file, FileHandler handler, boolean gzipped) throws IOException {
        try (TarArchiveInputStream tarStream = new TarArchiveInputStream(
                gzipped ? new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))
                        : new BufferedInputStream(new FileInputStream(file)))) {
            return read(tarStream, handler);
        }
    }

    protected Set<String> read(TarArchiveInputStream tarStream, FileHandler handler) throws IOException {
        Set<String> readFiles = new HashSet<>();
        TarArchiveEntry entry = tarStream.getNextTarEntry();
        while (entry != null) {
            if (entry.isFile()) {
                handler.handleStream(tarStream, entry.getName(), entry.getSize());
                readFiles.add(entry.getName());
            }
            // read the next entry
            entry = tarStream.getNextTarEntry();
        }
        return readFiles;
    }
}
