package org.dice_research.ldcbench.utils.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * A simple generator for a tar file based on a given, sorted Map.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class TarFileGenerator {

    /**
     * The easiest way to generate a tar file with the given file name, the given
     * list of files that will be added to the archive and a flag for creating a
     * compressed archive.
     * 
     * <p>The given Map is a sorted mapping from file names (including directories) the
     * files will have within the archive to real world files. For example, the
     * given Map could contain the entries {@code ("dir1/file0", "/home/file12")},
     * {@code ("dir1/file1", "/home/dir14/dir9/file99.txt")},
     * {@code ("README.md", "README.md")}. The key of the entries will define how
     * the archive itself will be structured. In the given example, there will be a
     * {@code "README.md"} file as well as a directory {@code "dir1"} in the root
     * directory of the archive. {@code "dir1"} will contain two file
     * {@code "file0"} and {@code "file1"}. The value of the entries defines where
     * the content of the files is taken from. It can be seen that these files can
     * be located in completely different locations.</p>
     * 
     * @param file  the name and path of the tar file that will be written by this
     *              method
     * @param files the mapping of internal file names to file paths used to read
     *              the files
     * @param useGZ if this flag is {@code true}, the tar file will be compressed
     *              using gzip.
     * @throws IOException is thrown if either the tar file can not be written or
     *                     one of the files in the given map can not be read.
     */
    public void generateTarFile(File file, SortedMap<String, File> files, boolean useGZ) throws IOException {
        // Create file with or without GZ stream
        try (TarArchiveOutputStream outStream = new TarArchiveOutputStream(
                useGZ ? new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
                        : new BufferedOutputStream(new FileOutputStream(file)))) {
            generate(files, outStream);
        }
    }

    /**
     * The method writes the files from the given sorted Mapping to the given output
     * stream.
     * 
     * <p>The given Map is a sorted mapping from file names (including directories) the
     * files will have within the archive to real world files. For example, the
     * given Map could contain the entries {@code ("dir1/file0", "/home/file12")},
     * {@code ("dir1/file1", "/home/dir14/dir9/file99.txt")},
     * {@code ("README.md", "README.md")}. The key of the entries will define how
     * the archive itself will be structured. In the given example, there will be a
     * {@code "README.md"} file as well as a directory {@code "dir1"} in the root
     * directory of the archive. {@code "dir1"} will contain two file
     * {@code "file0"} and {@code "file1"}. The value of the entries defines where
     * the content of the files is taken from. It can be seen that these files can
     * be located in completely different locations.</p>
     * 
     * @param files     the mapping of internal file names to file paths used to
     *                  read the files
     * @param outStream the output stream to which the files will be written.
     * @throws IOException is thrown if either the tar file can not be written or
     *                     one of the files in the given map can not be read.
     */
    public void generate(SortedMap<String, File> files, TarArchiveOutputStream outStream) throws IOException {
        for (Entry<String, File> e : files.entrySet()) {
            outStream.putArchiveEntry(outStream.createArchiveEntry(e.getValue(), e.getKey()));
            try (InputStream input = new BufferedInputStream(new FileInputStream(e.getValue()))) {
                IOUtils.copy(input, outStream);
            }
            outStream.closeArchiveEntry();
        }
    }

}
