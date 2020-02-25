package org.dice_research.ldcbench.benchmark.eval.supplier.bytes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TarGZBasedTTLModelIterator implements Iterator<Model>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarGZBasedTTLModelIterator.class);

    protected TarArchiveInputStream tarStream;
    protected Model model = null;

    public TarGZBasedTTLModelIterator(TarArchiveInputStream tarStream) {
        this.tarStream = tarStream;
    }

    @Override
    public synchronized boolean hasNext() {
        if(model == null) {
            readModel();
        }
        return model != null;
    }

    @Override
    public synchronized Model next() {
        hasNext();
        Model result = model;
        model = null;
        return result;
    }

    private void readModel() {
        try {
            TarArchiveEntry entry = tarStream.getNextTarEntry();
            while (entry != null) {
                if (entry.isFile()) {
                    Model localModel = ModelFactory.createDefaultModel();
                    localModel.read(tarStream, "", "TTL");
                    model = localModel;
                    return;
                }
                // read the next entry
                entry = tarStream.getNextTarEntry();
            }
        } catch (Exception e) {
            LOGGER.error("Exception while reading tar file. Returning null.", e);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(tarStream);
    }

    public static TarGZBasedTTLModelIterator create(File f) throws FileNotFoundException, IOException {
        return new TarGZBasedTTLModelIterator(
                new TarArchiveInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(f)))));
    }
}
