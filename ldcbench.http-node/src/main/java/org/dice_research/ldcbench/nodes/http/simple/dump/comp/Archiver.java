package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract Archiver which defines the attributes each archiver must have
 * and implement the logic used by all archivers to create Archives
 *
 */
public abstract class Archiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Archiver.class);

    /**
     * The media type string of the compression.
     */
    protected String mediaType;
    /**
     * Typical file name extension for this type of compression (including the '.' character).
     */
    protected String fileNameExtension;

	/**
	 * Create an Archive of the dumpFile using the outputstream of the Archiver
	 *
	 * @param dumpFile
	 * 			the dumpfile to put in the Archive
	 * @param archivename
	 * 			the name of the created archive
	 * @throws IOException
	 */
	public void buildArchive(File archive, File dumpFile) {
		ArchiveOutputStream aos = createStream(archive);
		if (dumpFile.isFile()) {
			addFileToArchive(aos, dumpFile);
		} else if (dumpFile.isDirectory()) { //Support directories?
			File[] Childrenfiles = dumpFile.listFiles();
			if (Childrenfiles != null) {
				for (File file : Childrenfiles) {
					addFileToArchive(aos, file);
				}
			}
		}
		try {
			aos.close();
		} catch (IOException e) {
			LOGGER.error("Failed closing ArchiveOutputStream");
		}
	}

	/**
	 * add a file to the archive
	 *
	 * @param dumpFile
	 * 			the file to add
	 */
	protected void addFileToArchive(ArchiveOutputStream aos, File dumpFile) {
		try {
			aos.putArchiveEntry(aos.createArchiveEntry(dumpFile, dumpFile.getName()));
			IOUtils.copy(new BufferedInputStream(new FileInputStream(dumpFile)), aos);
			aos.closeArchiveEntry();
		} catch (IOException e) {
			LOGGER.error("Failed creating Archive");
		}
	}

	/**
	 * initialize the outputStream and set everything up for the creation of the archive
	 *
	 * @param archive
	 * 			the name of the archive that will be created
	 *
	 */
	protected abstract ArchiveOutputStream createStream(File archive);

    public String getMediaType() {
        return mediaType;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

}