package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Archiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Archiver.class);

    /**
     * The class implementing the Archiver.
     */
    private Constructor<? extends ArchiveOutputStream> archiverConstructor;
    /**
     * The media type string of the compression.
     */
    private String mediaType;
    /**
     * Typical file name extension for this type of compression (including the '.' character).
     */
    private String fileNameExtension;

    public Archiver(Constructor<? extends ArchiveOutputStream> archiverConstructor, String mediaType,
            String fileNameExtension) {
        this.archiverConstructor = archiverConstructor;
        this.mediaType = mediaType;
        this.fileNameExtension = fileNameExtension;
    }

	@SuppressWarnings("unchecked")
	public static Archiver create(String className, String mediaType, String fileNameExtension) {
        try {
            return create((Class<? extends ArchiveOutputStream>) Class.forName(className), mediaType, fileNameExtension);
        } catch (ClassNotFoundException e) {
            LOGGER.info("Error while trying to find compression class. Returning null.", e);
            return null;
        }
	}

    public static Archiver create(Class<? extends ArchiveOutputStream> archiverClass, String mediaType,
            String fileNameExtension) {
        try {
            return new Archiver(
                    (Constructor<? extends ArchiveOutputStream>) archiverClass.getDeclaredConstructor(OutputStream.class),
                    mediaType, fileNameExtension);
        } catch (Exception e) {
            LOGGER.info("Error while trying to find compression class. Returning null.", e);
            return null;
        }
    }

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
	private ArchiveOutputStream createStream(File archive) {
		try {
			return archiverConstructor.newInstance(new BufferedOutputStream(new FileOutputStream(archive)));
		} catch (Exception e) {
			LOGGER.error("Couldn't create Archive Instance. Returning null. ", e);
			return null;
		}
	}

    public String getMediaType() {
        return mediaType;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

}