package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compression factory which relies on a the ZIP compression.
 * 
 * FIXME: This class has a huge issue since it needs the name of the file that
 * is compressed, internally. We fixed that with a workaround. However, this
 * workaround breaks the workflow and should be fixed in the future!
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class ZipStreamFactory implements CompressionStreamFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipStreamFactory.class);

    private static final String MEDIA_TYPE = "application/zip";
    private static final String FILE_NAME_EXTENSION = ".zip";
    /**
     * The name of the file that will be compressed by the next stream created by
     * this factory. FIXME This is a workaround and should be removed!
     */
    private String compressedFileName;

    /**
     * Constructor.
     */
    public ZipStreamFactory() {
    }

    @Override
    public OutputStream createCompressionStream(OutputStream os) {
        try {
            ZipOutputStream zout = new ZipOutputStream(os);
            zout.putNextEntry(new ZipEntry(compressedFileName));
            return zout;
        } catch (Exception e) {
            LOGGER.error("Couldn't create compression instance. Returning null.", e);
            return null;
        }
    }

    @Override
    public String getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public String getFileNameExtension() {
        return FILE_NAME_EXTENSION;
    }

    /**
     * Sets the name of the file that will be compressed by the next stream created
     * by this factory. FIXME This is a workaround and should be removed!
     * 
     * @param compressedFileName the name of the file that will be compressed by the
     *                           next stream created by this factory.
     */
    public void setCompressedFileName(String compressedFileName) {
        this.compressedFileName = compressedFileName;
    }
}
