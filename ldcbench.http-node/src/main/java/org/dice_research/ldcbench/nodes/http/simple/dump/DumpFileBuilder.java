package org.dice_research.ldcbench.nodes.http.simple.dump;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.simple.dump.comp.BrotliStreamFactory;
import org.dice_research.ldcbench.nodes.http.simple.dump.comp.CompressionStreamFactory;
import org.dice_research.ldcbench.nodes.http.simple.dump.comp.ReflectionBasedStreamFactory;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
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

    public static final List<CompressionStreamFactory> COMPRESSIONS = Arrays.asList(
            ReflectionBasedStreamFactory.create("java.util.zip.GZIPOutputStream", "application/gzip", ".gz"),
            //ReflectionBasedStreamFactory.create("java.util.zip.ZipOutputStream", "application/zip", ".zip"),
            ReflectionBasedStreamFactory.create(
                    "org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream",
                    "application/x-bzip2", ".bz2"),
            new BrotliStreamFactory());

    protected final int domainId;
    protected final String[] resourceUriTemplates;
    protected final String[] accessUriTemplates;
    protected final Graph[] graphs;
    protected final Lang lang;
    protected final CompressionStreamFactory compression;
    protected File dumpFile;

    public DumpFileBuilder(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            Lang lang, CompressionStreamFactory compression) {
        this.domainId = domainId;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.graphs = graphs;
        this.lang = lang;
        this.compression = compression;
    }

    public File build()
            throws IOException, NoSuchMethodException, SecurityException, ReflectiveOperationException, ParserException, NotFoundException {
        try (OutputStream out = generateOutputStream()) {
            streamData(out, lang);
        }
        getHDTStream(dumpFile, lang);
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
        dumpFile = File.createTempFile("ldcbench", ".dump");
        OutputStream out = new FileOutputStream(dumpFile);
        out = new BufferedOutputStream(out);
        if (compression != null) {
            out = compression.createCompressionStream(out);
        }
        return out;
    }

    private void streamData(OutputStream out, Lang lang) throws IOException {
        int datasetId = 0;
        StreamRDF writerStream = null;
        HDT hdt = null;
        
        File hdtTempFile = File.createTempFile("hdttemp", ".nt");
        OutputStream hdtout = new FileOutputStream(hdtTempFile);
        
        try {
            writerStream = StreamRDFWriter.getWriterStream(out, lang);
            
        } catch(RiotException e ) {
            if(e.getMessage().startsWith("No serialization for language")) {
                LOGGER.warn("No serialization for language Lang: {}. Trying to write it from an in-memory model.", lang);
                writeData(out, lang);
            } else {
                throw e;
            }
        }
        
        // The stream has been created
        if(writerStream != null) {
        writerStream.start();
        TripleIterator iterator;
        LOGGER.info("Domain ID: " + domainId);
        LOGGER.info("graph size: " + graphs.length);

        for(Graph graph: graphs) {
            for (int i = 0; i < graph.getNumberOfNodes(); ++i) {
                iterator = new TripleIterator(graphs, domainId, resourceUriTemplates, accessUriTemplates, datasetId, i);
                StreamOps.sendTriplesToStream(iterator, writerStream);
                //RDFDataMgr.write(hdtout, dataset, lang);
            }
            datasetId++;
        }
        writerStream.finish();
        
        
        LOGGER.info("writerStream: " + writerStream);
        }
    }

	private void getHDTStream(File dumpFile2, Lang lang2) throws IOException, ParserException, NotFoundException {
		
		// hdt codes from - https://www.rdfhdt.org/manual-of-the-java-hdt-library/
		
		//String rdfInput = "test.nt";
		String rdfInput = dumpFile2.getAbsolutePath();       //input file as string
		String hdtOutput = "testhdt.hdt";                   // output file 
		String baseURI = "http://domain0.org/dataset-0";
		String inputType = lang.getName().replaceAll("-", "").replace('/', '-');
		
		//LOGGER.info("temp : " + temp);
		
		HDT hdt = HDTManager.generateHDT(
                rdfInput,         // Input RDF File
                baseURI,          // Base URI - maybe it is empty for us?
                RDFNotation.parse(inputType), // Input Type
                new HDTSpecification(),   // HDT Options
                null              // Progress Listener
		);
		
		hdt.saveToHDT(hdtOutput, null);
		
		HDT printhdt = HDTManager.loadHDT(hdtOutput, null);
		
	    IteratorTripleString it = printhdt.search("", "", "");  //print hdt output in a readable format
	    while(it.hasNext()) {
	        TripleString ts = it.next();
	        LOGGER.info("Ts: "+ts);
	    }
	    
	    
	}

	private void writeData(OutputStream out, Lang lang) {
        TripleIterator iterator;
        LOGGER.info("Domain ID: " + domainId);
        LOGGER.info("graph size: " + graphs.length);
        
        Model model = ModelFactory.createDefaultModel();
        org.apache.jena.graph.Graph modelGraph = model.getGraph();
        int datasetId = 0;
        for(Graph graph: graphs) {
            for (int i = 0; i < graph.getNumberOfNodes(); ++i) {
                iterator = new TripleIterator(graphs, domainId, resourceUriTemplates, accessUriTemplates, datasetId, i);
                while(iterator.hasNext()) {
                    modelGraph.add(iterator.next());
                }
            }
            datasetId++;
        }
        if (lang.equals(Lang.RDFXML)) {
            // Just RDFXML leads to StackOverflow exceptions
            RDFDataMgr.write(out, model, RDFFormat.RDFXML_PLAIN);
        } else {
            RDFDataMgr.write(out, model, lang);
        }
    }

    public String buildContentType() {
        if (compression != null) {
            return compression.getMediaType();
        }
        return lang.getHeaderString();
    }

}
