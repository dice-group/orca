package org.dice_research.ldcbench.nodes.sparql.simple;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hobbit.core.Constants.CONTAINER_TYPE_BENCHMARK;

import org.apache.jena.graph.Triple;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.NodeComponent;
import org.dice_research.ldcbench.rdf.SimpleTripleCreator;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.sink.SparqlBasedSink;
import org.dice_research.ldcbench.util.uri.Constants;
import org.dice_research.ldcbench.util.uri.CrawleableUri;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Sparql Node
 *
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleSparqlComponent extends NodeComponent implements Component {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSparqlComponent.class);

    protected String sparqlContainer = null;

    private static final String SPARQL_IMG = "openlink/virtuoso-opensource-7:latest";

    private Sink sink;

    @Override
    public void initBeforeDataGeneration() throws Exception {
        LOGGER.debug("Starting SPARQL service: {}...", SPARQL_IMG);
        sparqlContainer = createContainer(SPARQL_IMG, CONTAINER_TYPE_BENCHMARK,
                new String[] { "DBA_PASSWORD=" + ApiConstants.SPARQL_PASSWORD });
        resourceUriTemplate = "http://" + sparqlContainer + "/data/%s-%s/%s-%s";
        accessUriTemplate = "http://" + sparqlContainer + ":8890/sparql";
        LOGGER.info("SPARQL service started at: {}", accessUriTemplate);
        
        Thread.sleep(15000);
        

        sink = SparqlBasedSink.create(accessUriTemplate + "-auth", ApiConstants.SPARQL_USER,
                ApiConstants.SPARQL_PASSWORD);
    }

    @Override
    public void initAfterDataGeneration() throws Exception {
        LOGGER.info("Adding triples to SPARQL database...");

        CrawleableUri uri = new CrawleableUri(new URI(accessUriTemplate));
        uri.addData(Constants.UUID_KEY, UUID.randomUUID().toString());
        SparqlBasedSink sbs = (SparqlBasedSink) sink;
        sbs.deleteTriples();
        sink.openSinkForUri(uri);

        SimpleTripleCreator tripleCreator = new SimpleTripleCreator(
            cloudNodeId.get(),
            Stream.of(nodeMetadata).map(nm -> nm.getResourceUriTemplate()).toArray(String[]::new),
            Stream.of(nodeMetadata).map(nm -> nm.getAccessUriTemplate()).toArray(String[]::new)
        );

        int triples = 0;
        for (Graph graph : graphs) {
            int nodes = graph.getNumberOfNodes();
            for (int node = 0; node < nodes; node++) {
                int[] types = graph.outgoingEdgeTypes(node);
                int[] targets = graph.outgoingEdgeTargets(node);
                int edges = targets.length;
                for (int edge = 0; edge < edges; edge++) {
                    Triple t = tripleCreator.createTriple(node, types[edge], targets[edge], graph.getExternalNodeId(targets[edge]), graph.getGraphId(targets[edge]));
                    LOGGER.debug("Triple: {}", t);
                    sink.addTriple(uri, t);
                    triples++;
                }
            }
        }
        
        sink.closeSinkForUri(uri);

        graphs = null;

        LOGGER.debug("Added {} triples.", triples);
    }

    @Override
    public void close() throws IOException {
        if (sparqlContainer != null) {
            LOGGER.info("Stopping Sparql Container {}", sparqlContainer);
            stopContainer(sparqlContainer);
        } else {
            LOGGER.info("There is no Sparql Container to stop.");
        }
        super.close();
    }

}
