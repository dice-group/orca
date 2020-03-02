package org.dice_research.ldcbench.utils.tar;

import java.io.IOException;
import java.io.InputStream;

/**
 * This simple interface is used by the {@link TarFileReader} to forward the
 * processing of single files to a different class.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface FileHandler {

    /**
     * This method is called for processing a single file within a tar file. The
     * processing class receives the input stream, from which the content of the
     * file can be read, the name of the file within the tar archive and the size of
     * the file (in bytes). Note that the method is not allowed to read more bytes
     * than the given numbe of bytes.
     * 
     * @param in   the input stream, from which the content of the file can be read
     * @param name the name of the file within the tar archive
     * @param size the size of the file (in bytes). At the same time, this is the
     *             maximum number of bytes the method is allowed to read from the
     *             given stream
     * @throws IOException if an IO error occurs when reading from the given stream
     */
    void handleStream(InputStream in, String name, long size) throws IOException;

}
