package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.benchmark.eval.sparql.CkanQueryPatternCreator;
import org.dice_research.ldcbench.benchmark.eval.sparql.QueryPatternCreator;
import org.dice_research.ldcbench.benchmark.eval.sparql.SimpleQueryPatternCreator;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphBasedTripleBlockStreamCreator implements TripleBlockStreamCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphBasedTripleBlockStreamCreator.class);

    @Override
    public boolean accepts(int graphId, String graphFile, String[] resourceUriTemplates, String[] accessUriTemplates) {
        return graphFile.endsWith(ApiConstants.FILE_ENDING_GRAPH);
    }

    @Override
    public Stream<ElementTriplesBlock> createStream(int graphId, String graphFile, String[] resourceUriTemplates,
            String[] accessUriTemplates) {
        try {
            Graph graph = SerializationHelper.deserialize(FileUtils.readFileToByteArray(new File(graphFile)));
            QueryPatternCreator creator;
            if (accessUriTemplates[graphId].matches(".*:5000/")) {
                LOGGER.debug("Using CKAN pattern creator to validate results from graph {}", graphId);
                creator = new CkanQueryPatternCreator(graphId, resourceUriTemplates, accessUriTemplates);
            } else {
                LOGGER.debug("Using Simple pattern creator to validate results from graph {}", graphId);
                creator = new SimpleQueryPatternCreator(graphId, resourceUriTemplates, accessUriTemplates);
            }
            return createStreamForGraph(graph, creator);
        } catch (Exception e) {
            LOGGER.error("Couldn't load graph " + Arrays.toString(new String[] { Integer.toString(graphId), graphFile,
                    resourceUriTemplates[graphId], accessUriTemplates[graphId] }) + ". Returning null.", e);
        }
        return null;
    }
    
    public static Stream<ElementTriplesBlock> createStreamForGraph(Graph graph, QueryPatternCreator creator) {
        return IntStream.range(0, graph.getNumberOfNodes()).parallel()
                .mapToObj(n -> createStreamForNode(n, graph, creator)).flatMap(s -> s);
    }

    public static Stream<ElementTriplesBlock> createStreamForNode(int node, Graph graph, QueryPatternCreator creator) {
        final int[] edgeTypes = graph.outgoingEdgeTypes(node);
        final int[] edgeTargets = graph.outgoingEdgeTargets(node);
        return IntStream.range(0, edgeTypes.length).mapToObj(e -> creator.create(node, edgeTypes[e], edgeTargets[e],
                graph.getExternalNodeId(edgeTargets[e]), graph.getGraphId(edgeTargets[e]), graph.getNodeType(edgeTargets[e])));
    }

}
