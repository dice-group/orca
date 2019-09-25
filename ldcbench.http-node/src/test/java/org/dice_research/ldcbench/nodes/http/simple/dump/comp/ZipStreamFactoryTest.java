package org.dice_research.ldcbench.nodes.http.simple.dump.comp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ZipStreamFactoryTest {

    protected static final int DATA_SIZE = 1000;
    protected static final String COMPRESSED_FILE_NAME = "data.dat";

    protected String compressionClassName;
    protected String decompressionClassName;

    @Test
    public void test() throws IOException {
        ZipStreamFactory factory = new ZipStreamFactory();
        factory.setCompressedFileName(COMPRESSED_FILE_NAME);
        byte[] data = ReflectionBasedStreamFactoryTest.generateData();

        // Compress the data
        ByteArrayOutputStream bArrayOStream = new ByteArrayOutputStream(2 * DATA_SIZE);
        try (OutputStream os = factory.createCompressionStream(bArrayOStream)) {
            os.write(data);
        }
        // Read and decompress the data again
        ByteArrayInputStream bArrayIStream = new ByteArrayInputStream(bArrayOStream.toByteArray());

        int length = 0;
        byte redData[] = new byte[2 * DATA_SIZE];
        try (InputStream is = generateInputStream(bArrayIStream)) {
            length = IOUtils.read(is, redData);
        } catch (Exception e) {
            throw new AssertionError("Unexpected Exception.", e);
        }
        redData = Arrays.copyOf(redData, length);
        Assert.assertArrayEquals(data, redData);
    }

    private InputStream generateInputStream(InputStream is) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(is);
        zipIn.getNextEntry();
        return zipIn;
    }
}
