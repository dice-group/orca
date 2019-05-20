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

    public SparqlResource(int domainId, String[] domains, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes,Sink sink) {
        super(domainId, domains, graphs, predicate, contentTypes);
        this.sink = sink;
    }


    public void storeGraphs(String domain) throws Exception {

        URI uri = null;
        LOGGER.info("Storing relation for: " + domain);
        try {
            uri = new URI(domain);
            sink.openSinkForUri(new CrawleableUri(uri));
            int ids[] = parseIds(domain);
            TripleIterator iterator = new TripleIterator(this, ids[0], ids[1]);
        	LOGGER.info("Starting storing triples for sparqlResource");
            while (iterator.hasNext()) {
            	Triple t = iterator.next();
            	LOGGER.info("Triple: " + t.toString());
                sink.addTriple(new CrawleableUri(uri), t);
                iterator.next();  
            }
        } catch (Exception e) {

        }finally {
            sink.closeSinkForUri(new CrawleableUri(uri));
        }
    }

}
