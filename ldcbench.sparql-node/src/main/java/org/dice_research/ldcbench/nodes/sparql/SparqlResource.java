package org.dice_research.ldcbench.nodes.sparql;

import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.simple.GraphBasedResource;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.sink.Sink;
import org.simpleframework.http.Request;

public class SparqlResource extends GraphBasedResource {
    
 private Sink sink;

    public SparqlResource(int domainId, String[] domains, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes,Sink sink) {
        super(domainId, domains, graphs, predicate, contentTypes);
        this.sink = sink;
    }

    @Override
    public boolean handleRequest(String target, Lang lang, String charset, OutputStream out) {
        
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
        return true;
    }

    public List<Triple> generateTripleList(String target) throws Exception {

        int ids[] = parseIds(target);
        TripleIterator iterator = new TripleIterator(this, ids[0], ids[1]);

        List<Triple> listTriples = new ArrayList<Triple>();

        while (iterator.hasNext()) {
            listTriples.add(iterator.next());
        }
        return listTriples;
    }

}
