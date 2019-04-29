package org.dice_research.ldcbench.nodes.sparql.simple;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import org.dice_research.ldcbench.nodes.http.simple.GraphBasedResource;
import org.dice_research.ldcbench.nodes.sparql.SparqlResource;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.sink.Sink;
import org.dice_research.squirrel.sink.impl.sparql.SparqlBasedSink;
import org.hobbit.core.components.Component;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Sparql Node
 * 
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleSparqlComponent extends AbstractNodeComponent implements Component {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSparqlComponent.class);
	
	protected String sparqlContainer = null;

	private static final String SPARQL_IMG = "openlink/virtuoso-opensource-7:latest";
	
	private Sink sink;
	
	@Override
	    public void init() throws Exception {
	        // TODO Auto-generated method stub
	        super.init();
	        
	        sparqlContainer = createContainer(SPARQL_IMG, new String[] { "DBA_PASSWORD="+ApiConstants.SPARQL_PASSWORD });
	        
	        
	        sink = SparqlBasedSink.create("http://"+ sparqlContainer + ":8890/sparql-auth",
	                ApiConstants.SPARQL_USER,ApiConstants.SPARQL_PASSWORD);
	        
	        
	        LOGGER.info("Sparql server initialized.");
	        
	        storeGraphs(graphs);
        
	        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);
	    }
	
	protected void storeGraphs(List<Graph> listGraphs) {
	    SparqlResource resource = new SparqlResource(domainId, domainNames,
	            listGraphs.toArray(new Graph[listGraphs.size()]), (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
                        && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)), new String[] {},sink);
	    
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
