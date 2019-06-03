package org.dice_research.ldcbench.nodes.sparql;

import java.net.URI;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.simple.GraphBasedResource;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.util.uri.CrawleableUri;
import org.simpleframework.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlResource extends GraphBasedResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlResource.class);
	
    
 private Sink sink;

    public SparqlResource(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes,Sink sink) {
        super(domainId, resourceUriTemplates, accessUriTemplates, graphs, predicate, contentTypes);
        this.sink = sink;
    }


    public void storeGraphs(int myId, String crawleableUri, int nodeId, String uriTemplate) throws Exception {
        CrawleableUri uri = null;
        LOGGER.info("Storing relation for: " + uriTemplate);
        try {
            uri = new CrawleableUri(new URI(uriTemplate).resolve("/"));
            sink.openSinkForUri(uri);

            int ids[] = parseIds(uriTemplate);
            TripleIterator iterator = new TripleIterator(this, nodeId, ids[1]);
        	LOGGER.info("Starting storing triples for sparqlResource");
            while (iterator.hasNext()) {
            	Triple t = iterator.next();
            	LOGGER.info("Triple: " + t.toString());
                sink.addTriple(uri, t);
                iterator.next();
            }
        } catch (Exception e) {

        }finally {
            sink.closeSinkForUri(uri);
        }
    }

}
