package org.dice_research.ldcbench.nodes.http.simple.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;

/**
 * A simple class which builds a dump file from the given graph by serializing
 * all triples of the graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class DumpFileBuilder {

    public static final Lang DEFAULT_LANG = Lang.TTL;

    protected final int domainId;
    protected final String[] resourceUriTemplates;
    protected final String[] accessUriTemplates;
    protected final Graph[] graphs;
    protected final Lang lang;
    protected final boolean useCompression;
    protected File dumpFile;

    public DumpFileBuilder(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs) {
        this(domainId, resourceUriTemplates, accessUriTemplates, graphs, DEFAULT_LANG, false);
    }

    public DumpFileBuilder(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            Lang lang, boolean useCompression) {
        this.domainId = domainId;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.graphs = graphs;
        this.lang = lang;
        this.useCompression = useCompression;
    }

    public File build() throws IOException {
        try (OutputStream out = generateOutputStream(lang, useCompression)) {
            streamData(out, lang);
        }
        return dumpFile;
    }

    private OutputStream generateOutputStream(Lang lang, boolean useCompression) throws FileNotFoundException, IOException {
//        StringBuilder fileNameBuilder = new StringBuilder();
//        fileNameBuilder.append("");
//        List<String> fileExt = lang.getFileExtensions();
//        if (fileExt.size() > 0) {
//            fileNameBuilder.append('.');
//            fileNameBuilder.append(fileExt.get(0));
//        }
        dumpFile = File.createTempFile("ldcbench", ".dump");
        OutputStream out = new FileOutputStream(dumpFile);
        out = new BufferedOutputStream(out);
        if(useCompression) {
            out = new GZIPOutputStream(out); 
        }
        return out;
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
        if(useCompression) {
            return "application/gzip";
        }
        return lang.getHeaderString();
    }

}
