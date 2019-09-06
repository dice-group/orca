package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.OutputStream;

/**
 * A simple interface for a factory that creates compression streams and gives
 * additional meta data about the compression used.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface CompressionStreamFactory {

    /**
     * Creates an output stream by wrapping the given output stream using the
     * compression method of this factory.
     * 
     * @param os
     *            the output stream supplying the data that should be compressed
     * @return a stream with the given data using the compression of this factory
     */
    public OutputStream createCompressionStream(OutputStream os);

    /**
     * Returns the media type of the used compression. In the ideal case, this type
     * is following the <a href=
     * "https://www.iana.org/assignments/media-types/media-types.xhtml">definitions
     * of the IANA</a>.
     * 
     * @return the media type of the used compression.
     */
    public String getMediaType();
}
