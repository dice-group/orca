package org.dice_research.ldcbench.nodes.sparql;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.sink.SparqlBasedSink;
import org.dice_research.ldcbench.util.uri.Constants;
import org.dice_research.ldcbench.util.uri.CrawleableUri;

public class TestSink {
    
    
    public static void main(String[] args) throws URISyntaxException {
        CrawleableUri curi = new CrawleableUri(new URI("http://test.de"));
        
        curi.addData(Constants.UUID_KEY, UUID.randomUUID().toString());
        
        Sink sink = SparqlBasedSink.create("http://localhost:8890/sparql-auth",
                "dba", "pw123");
        
        SparqlBasedSink sbs = (SparqlBasedSink) sink;
        
        sbs.deleteTriples();
        
        Triple t1 = new Triple(NodeFactory.createURI("http://test.de/resource1"),
                NodeFactory.createURI("is"),
                NodeFactory.createLiteral("triple1"));
        
        Triple t2 = new Triple(NodeFactory.createURI("http://test.de/resource2"),
                NodeFactory.createURI("is"),
                NodeFactory.createLiteral("triple2"));
        
        sink.openSinkForUri(curi);
        
        sink.addTriple(curi, t1);
        sink.addTriple(curi, t2);
        
        sink.closeSinkForUri(curi);
        
    }

}
