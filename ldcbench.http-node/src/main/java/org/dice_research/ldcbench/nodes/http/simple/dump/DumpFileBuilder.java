package org.dice_research.ldcbench.nodes.http.simple.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.simple.dump.comp.CompressionStreamFactory;
import org.dice_research.ldcbench.nodes.http.simple.dump.comp.ReflectionBasedStreamFactory;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.collections.Collections;

/**
 * A simple class which builds a dump file from the given graph by serializing
 * all triples of the graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class DumpFileBuilder {

    public static final Lang DEFAULT_LANG = Lang.TTL;

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileBuilder.class);

    private static final List<CompressionStreamFactory> COMPRESSIONS = Arrays.asList(
            ReflectionBasedStreamFactory.create("java.util.zip.GZIPOutputStream", "application/gzip"),
            ReflectionBasedStreamFactory.create("java.util.zip.ZipOutputStream", "application/zip"),
            ReflectionBasedStreamFactory.create(
                    "org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream",
                    "application/x-bzip2"));

    protected final String defaultCompressionType = "java.util.zip.GZIPOutputStream";
    protected final int domainId;
    protected final String[] resourceUriTemplates;
    protected final String[] accessUriTemplates;
    protected final Graph[] graphs;
    protected final Lang lang;
    protected final CompressionStreamFactory compression;
    protected File dumpFile;
    private Random rand;

    public DumpFileBuilder(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            long seed) {
        this(domainId, resourceUriTemplates, accessUriTemplates, graphs, DEFAULT_LANG, seed);
    }

    public DumpFileBuilder(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            Lang lang, long seed) {
        rand = new Random(seed);
        this.domainId = domainId;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.graphs = graphs;
        this.lang = lang;
        compression = randomCompressionType();
        if (compression == null) {
            LOGGER.info("Created dump file builder without compression.");
        }
    }

    public File build()
            throws IOException, NoSuchMethodException, SecurityException, ReflectiveOperationException {
        try (OutputStream out = generateOutputStream()) {
            streamData(out, lang);
        }
        return dumpFile;
    }

    @SuppressWarnings("resource")
    private OutputStream generateOutputStream()
            throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // StringBuilder fileNameBuilder = new StringBuilder();
        // fileNameBuilder.append("");
        // List<String> fileExt = lang.getFileExtensions();
        // if (fileExt.size() > 0) {
        // fileNameBuilder.append('.');
        // fileNameBuilder.append(fileExt.get(0));
        // }
        dumpFile = File.createTempFile("", ".dump");
        OutputStream out = new FileOutputStream(dumpFile);
        out = new BufferedOutputStream(out);
        if (compression != null) {
            out = compression.createCompressionStream(out);
        }
        return out;
    }

    private CompressionStreamFactory randomCompressionType() {
        return Collections.pickRandomObject(COMPRESSIONS, rand);

    }

    private void streamData(OutputStream out, Lang lang) {
        int datasetId = 0;
        StreamRDF writerStream = StreamRDFWriter.getWriterStream(out, lang);
        writerStream.start();
        TripleIterator iterator;
        int numberOfNodes = graphs[domainId].getNumberOfNodes();
        for (int i = 0; i < numberOfNodes; ++i) {
            iterator = new TripleIterator(graphs, domainId, resourceUriTemplates, accessUriTemplates, datasetId, i);
            StreamOps.sendTriplesToStream(iterator, writerStream);
        }
        writerStream.finish();
    }

    public String buildContentType() {
        if (compression != null) {
            return compression.getMediaType();
        }
        return lang.getHeaderString();
    }

}
