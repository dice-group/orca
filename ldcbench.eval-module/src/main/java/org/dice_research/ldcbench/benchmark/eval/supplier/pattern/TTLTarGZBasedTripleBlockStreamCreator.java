package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.eval.sparql.QueryPatternCreator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.utils.tar.TarGZBasedTTLModelIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTLTarGZBasedTripleBlockStreamCreator implements TripleBlockStreamCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TTLTarGZBasedTripleBlockStreamCreator.class);

    @Override
    public boolean accepts(int graphId, String graphFile, String[] resourceUriTemplates, String[] accessUriTemplates) {
        return graphFile.endsWith(ApiConstants.FILE_ENDING_TTL_TAR_GZ);
    }

    @Override
    public Stream<ElementTriplesBlock> createStream(int graphId, String graphFile, String[] resourceUriTemplates,
            String[] accessUriTemplates) {
        try {
            TarGZBasedTTLModelIterator iterator = TarGZBasedTTLModelIterator.create(new File(graphFile));
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                    .map(TTLTarGZBasedTripleBlockStreamCreator::createBlock);
        } catch (Exception e) {
            LOGGER.error("Couldn't load graph " + Arrays.toString(new String[] { Integer.toString(graphId), graphFile,
                    resourceUriTemplates[graphId], accessUriTemplates[graphId] }) + ". Returning null.", e);
        }
        return null;
    }

    protected static ElementTriplesBlock createBlock(Model model) {
        ElementTriplesBlock pattern = new ElementTriplesBlock();
        Iterator<Triple> iterator = model.getGraph().find();
        while (iterator.hasNext()) {
            pattern.addTriple(iterator.next());
        }
        return pattern;
    }

    protected Stream<ElementTriplesBlock> createStreamForNode(int node, Graph graph, QueryPatternCreator creator) {
        final int[] edgeTypes = graph.outgoingEdgeTypes(node);
        final int[] edgeTargets = graph.outgoingEdgeTargets(node);
        return IntStream.range(0, edgeTypes.length).mapToObj(e -> creator.create(node, edgeTypes[e], edgeTargets[e],
                graph.getExternalNodeId(edgeTargets[e]), graph.getGraphId(edgeTargets[e])));
    }

}
