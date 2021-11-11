package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ArchiverTest {

    protected static final int DATA_SIZE = 1000;

    protected Archiver archiver;
    protected String unarchiverClassName;
    protected String decompressionClassName;

    public ArchiverTest(Archiver archiver, String unarchiverClassName, String decompressionClassName) {
        this.archiver = archiver;
        this.unarchiverClassName = unarchiverClassName;
        this.decompressionClassName = decompressionClassName;
    }

    @Parameters
    public static List<Object[]> testCases() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] {new TarArchiver(),
                "org.apache.commons.compress.archivers.tar.TarArchiveInputStream", null});
        data.add(new Object[] {new TarArchiver(
                ReflectionBasedStreamFactory.create("java.util.zip.GZIPOutputStream", "application/gzip", ".gz")),
                "org.apache.commons.compress.archivers.tar.TarArchiveInputStream",
                "java.util.zip.GZIPInputStream"});
        data.add(new Object[] {new ZipArchiver(),
                "org.apache.commons.compress.archivers.zip.ZipArchiveInputStream", null});
        return data;
    }

    @Test
    public void test() throws IOException {
        byte[] data = generateData();
        
        //archive
        File fileToArchive = File.createTempFile("dummy", ".dump");
        FileUtils.writeByteArrayToFile(fileToArchive, data);
        File archive = File.createTempFile("dummy", ".archive");
        archiver.buildArchive(archive, fileToArchive);
        
        //archive multi
        File file1 = File.createTempFile("dummy1", ".dump");
        FileUtils.writeByteArrayToFile(file1, data);
        File file2 = File.createTempFile("dummy2", ".dump");
        FileUtils.writeByteArrayToFile(file2, data);
        File file3 = File.createTempFile("dummy3", ".dump");
        FileUtils.writeByteArrayToFile(file3, data);
        File multiArchive = File.createTempFile("multiDump", ".archive");
        ArchiveOutputStream aos =  archiver.createStream(multiArchive);
        archiver.addFileToArchive(aos, file1);
        archiver.addFileToArchive(aos, file2);
        archiver.addFileToArchive(aos, file3);
        
        //unarchive
        FileInputStream fis = new FileInputStream(archive);
        ArchiveInputStream ais;
        File unarchivedFile = null;
		try {
			ais = generateInputStream(new BufferedInputStream(fis), unarchiverClassName, decompressionClassName);
			ais.getNextEntry();
			unarchivedFile = File.createTempFile("dummy", ".unarchived");
			IOUtils.copy(ais, new FileOutputStream(unarchivedFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//unarchive multi
		FileInputStream fisMulti = new FileInputStream(multiArchive);
        ArchiveInputStream aisMulti;
        File unarchivedFile1 = null;
        File unarchivedFile2 = null;
        File unarchivedFile3 = null;
		try {
			aisMulti = generateInputStream(new BufferedInputStream(fisMulti), unarchiverClassName, decompressionClassName);
			aisMulti.getNextEntry();
			unarchivedFile1 = File.createTempFile("file1", ".unarchived");
			IOUtils.copy(aisMulti, new FileOutputStream(unarchivedFile1));
			unarchivedFile2 = File.createTempFile("file2", ".unarchived");
			IOUtils.copy(aisMulti, new FileOutputStream(unarchivedFile2));
			unarchivedFile3 = File.createTempFile("file3", ".unarchived");
			IOUtils.copy(aisMulti, new FileOutputStream(unarchivedFile3));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        assertTrue(FileUtils.contentEquals(fileToArchive, unarchivedFile));
        assertTrue(FileUtils.contentEquals(file1, unarchivedFile1));
        assertTrue(FileUtils.contentEquals(file2, unarchivedFile2));
        assertTrue(FileUtils.contentEquals(file3, unarchivedFile3));
    }

    protected static byte[] generateData() {
        long seed = System.currentTimeMillis();
        System.out.println("Using " + seed + " as seed to generate data.");
        Random random = new Random(seed);
        byte[] data = new byte[DATA_SIZE];
        random.nextBytes(data);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static ArchiveInputStream generateInputStream(InputStream is, String archiverClassName,
            String decompressionClassName)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<? extends ArchiveInputStream> clazz = (Class<? extends ArchiveInputStream>) Class.forName(archiverClassName);
        Constructor<? extends ArchiveInputStream> constructor = (Constructor<? extends ArchiveInputStream>) clazz
                .getDeclaredConstructor(InputStream.class);
        if( decompressionClassName != null) {
            Class<? extends InputStream> decClazz = (Class<? extends InputStream>) Class.forName(decompressionClassName);
            Constructor<? extends InputStream> decConstructor= (Constructor<? extends InputStream>) decClazz
                    .getDeclaredConstructor(InputStream.class);
            return constructor.newInstance(decConstructor.newInstance(is));
        }
        return constructor.newInstance(is);
    }
}
