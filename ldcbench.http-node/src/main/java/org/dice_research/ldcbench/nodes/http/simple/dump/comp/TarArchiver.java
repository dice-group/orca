package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TarArchiver extends Archiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarArchiver.class);

    private static final String TAR_MEDIA_TYPE = "application/x-tar";
    private static final String TAR_GZIP_MEDIA_TYPE = "application/x-gtar";
    private static final String TAR_FILE_NAME_EXTENSION = ".tar";
    private static final String TAR_GZIP_FILE_NAME_EXTENSION = "tar.gz";

    private boolean gziped;

    public static TarArchiver createArchiver() {
        return new TarArchiver(false);
    }

    public static TarArchiver createArchiverWithGzip() {
        return new TarArchiver(true);
    }

    public TarArchiver(boolean gziped) {
        this.gziped = gziped;
        mediaType = gziped ? TAR_GZIP_MEDIA_TYPE : TAR_MEDIA_TYPE;
        fileNameExtension = gziped ? TAR_GZIP_FILE_NAME_EXTENSION : TAR_FILE_NAME_EXTENSION;
    }


	@Override
	protected TarArchiveOutputStream createStream(File archive) {
		try {
			return new TarArchiveOutputStream(
					this.gziped ? new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(archive)))
							: new BufferedOutputStream(new FileOutputStream(archive)));
		} catch (Exception e) {
			LOGGER.error("Couldn't create Archive Instance. Returning null. ", e);
			return null;
		}
	}

}
