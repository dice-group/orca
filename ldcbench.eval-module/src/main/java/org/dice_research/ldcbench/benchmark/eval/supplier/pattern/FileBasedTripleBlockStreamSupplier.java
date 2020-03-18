package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

import java.util.stream.Stream;

import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates streams of triple blocks that can be used to check the completeness
 * of the single graphs provided as files. Internally, it makes use of
 * {@link TripleBlockStreamCreator} implementations. Note that it will only be
 * able to handle files which are accepted by at least one of these creators.
 * Note that the creators are used within the order they are provided, i.e., the
 * first creator that accepts a given file is used regardless whether another
 * creator would fit better.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class FileBasedTripleBlockStreamSupplier implements TripleBlockStreamSupplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedTripleBlockStreamSupplier.class);

    protected String[] graphFiles;
    protected String[] resourceUriTemplates;
    protected String[] accessUriTemplates;
    protected TripleBlockStreamCreator creators[];

    public FileBasedTripleBlockStreamSupplier(String[] graphFiles, String[] resourceUriTemplates,
            String[] accessUriTemplates, TripleBlockStreamCreator... creators) {
        this.graphFiles = graphFiles;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.creators = creators;
    }

    @Override
    public int getNumberOfGraphs() {
        return graphFiles.length;
    }

    @Override
    public Stream<ElementTriplesBlock> getTripleBlocks(int graphId) {
        for (int i = 0; i < creators.length; ++i) {
            if (creators[i].accepts(graphId, graphFiles[graphId], resourceUriTemplates, accessUriTemplates)) {
                return creators[i].createStream(graphId, graphFiles[graphId], resourceUriTemplates, accessUriTemplates);
            }
        }
        LOGGER.error("Couldn't find a matching creator for graph {},{},{},{}. Returning null.", graphId,
                graphFiles[graphId], resourceUriTemplates[graphId], accessUriTemplates[graphId]);
        return null;
    }

}
