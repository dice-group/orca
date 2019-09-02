package org.dice_research.ldcbench.nodes.http.simple.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    protected final String defaultCompressionType = "java.util.zip.GZIPOutputStream";
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

    public File build() throws IOException, NoSuchMethodException, SecurityException,  java.lang.ReflectiveOperationException {
        try (OutputStream out = generateOutputStream(lang, useCompression)) {
            streamData(out, lang);
        }
        return dumpFile;
    }

    @SuppressWarnings("resource")
    private OutputStream generateOutputStream(Lang lang, boolean useCompression) throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        StringBuilder fileNameBuilder = new StringBuilder();
//        fileNameBuilder.append("");
//        List<String> fileExt = lang.getFileExtensions();
//        if (fileExt.size() > 0) {
//            fileNameBuilder.append('.');
//            fileNameBuilder.append(fileExt.get(0));
//        }
        dumpFile = File.createTempFile("", ".dump");
        OutputStream out = new FileOutputStream(dumpFile);
        out = new BufferedOutputStream(out);
        if(useCompression) {
            Class<?> outputstream = randomCompressionType();
            
//            out = new GZIPOutputStream(out);
              out = (OutputStream) ((Object) outputstream.getClass().getDeclaredConstructor(OutputStream.class).newInstance(out));
        }
        return out;
    }
    
    private Class<?> randomCompressionType() {
        List<Class<?>> allowdCompressionTypesList = getAllowedCompressionTypes();
        Random rand = new Random();
        return allowdCompressionTypesList.get(rand.nextInt(allowdCompressionTypesList.size()));
        
    }
    
    private List<Class<?>> getAllowedCompressionTypes(){
        
        List<Class<?>> listAllowedCompressionTypes = new ArrayList<Class<?>>();
        try {

            listAllowedCompressionTypes.add(Class.forName("java.util.zip.GZIPOutputStream"));
            listAllowedCompressionTypes.add(Class.forName("java.util.zip.ZipOutputStream"));
            listAllowedCompressionTypes.add(Class.forName("org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream"));


        } catch (ClassNotFoundException e) {
            LOGGER.info(e.getMessage());
            LOGGER.info("Using the Default Compression Type");
            try {
                listAllowedCompressionTypes.add(Class.forName(defaultCompressionType));
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        
        return listAllowedCompressionTypes;
        
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
