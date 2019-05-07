package org.dice_research.ldcbench.nodes.sparql;

import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.simple.GraphBasedResource;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.util.uri.CrawleableUri;
import org.simpleframework.http.Request;

public class SparqlResource extends GraphBasedResource {
    
 private Sink sink;

    public SparqlResource(int domainId, String[] domains, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes,Sink sink) {
        super(domainId, domains, graphs, predicate, contentTypes);
        this.sink = sink;
    }


    public void storeGraphs(List<Graph> graphs, String target) throws Exception {

        URI uri = null;

        try {
            uri = new URI(target);
            sink.openSinkForUri(new CrawleableUri(uri));
            int ids[] = parseIds(target);
            TripleIterator iterator = new TripleIterator(this, ids[0], ids[1]);
            while (iterator.hasNext()) {
                sink.addTriple(new CrawleableUri(uri), iterator.next());
                iterator.next();  
            }
        } catch (Exception e) {

        }finally {
            sink.closeSinkForUri(new CrawleableUri(uri));
        }
    }

}
