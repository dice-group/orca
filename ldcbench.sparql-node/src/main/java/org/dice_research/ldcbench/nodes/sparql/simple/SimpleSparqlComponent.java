package org.dice_research.ldcbench.nodes.sparql.simple;

import java.io.IOException;

import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import org.dice_research.ldcbench.nodes.sparql.SparqlResource;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.sink.SparqlBasedSink;
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
	        
	        SparqlResource resource = new SparqlResource(domainId, domainNames,
	                graphs.toArray(new Graph[graphs.size()]), (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
	                        && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)), new String[] {},sink);
	        
	        resource.storeGraphs(graphs,sparqlContainer);
        
	        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);
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
