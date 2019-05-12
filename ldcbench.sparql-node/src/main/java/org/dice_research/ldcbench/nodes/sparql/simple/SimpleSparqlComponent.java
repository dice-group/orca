package org.dice_research.ldcbench.nodes.sparql.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static org.hobbit.core.Constants.CONTAINER_TYPE_BENCHMARK;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import org.dice_research.ldcbench.nodes.sparql.SparqlResource;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.dice_research.ldcbench.sink.Sink;
import org.dice_research.ldcbench.sink.SparqlBasedSink;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 
 * Sparql Node
 * 
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleSparqlComponent extends AbstractNodeComponent implements Component {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSparqlComponent.class);
	
	protected Channel bcBroadcastChannel;
	protected String sparqlContainer = null;
	protected SparqlResource resource;

	private static final String SPARQL_IMG = "openlink/virtuoso-opensource-7:latest";
	
	private Sink sink;
	
	@Override
	    public void init() throws Exception {
	        // TODO Auto-generated method stub
	        super.init();
	        
	        
	     // initialize exchange with BC
			String exchangeName = EnvVariables.getString(ApiConstants.ENV_BENCHMARK_EXCHANGE_KEY);
			bcBroadcastChannel = cmdQueueFactory.getConnection().createChannel();
			String queueName = bcBroadcastChannel.queueDeclare().getQueue();
			bcBroadcastChannel.exchangeDeclare(exchangeName, "fanout", false, true, null);
			bcBroadcastChannel.queueBind(queueName, exchangeName, "");

			Consumer consumer = new DefaultConsumer(bcBroadcastChannel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					try {
						handleBCMessage(body);
					} catch (Exception e) {
						LOGGER.error("Exception while trying to handle incoming command.", e);
					}
				}
			};
			bcBroadcastChannel.basicConsume(queueName, true, consumer);
	        
	        sparqlContainer = createContainer(SPARQL_IMG, CONTAINER_TYPE_BENCHMARK, new String[] { "DBA_PASSWORD="+ApiConstants.SPARQL_PASSWORD });
	        sink = SparqlBasedSink.create("http://"+ sparqlContainer + ":8890/sparql-auth",
	                ApiConstants.SPARQL_USER,ApiConstants.SPARQL_PASSWORD);
	        
	        
	        LOGGER.info("Sparql server initialized.");
	        
	        
	        
//	        resource.storeGraphs(graphs,sparqlContainer);
        
	        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);
	        
	        domainNamesReceived.acquire();
	        dataGenerationFinished.acquire();
	    }
	
	
	@Override
	public void receiveCommand(byte command, byte[] data) {
		switch (command) {
		case Commands.DATA_GENERATION_FINISHED:
			LOGGER.debug("Received DATA_GENERATION_FINISHED");
			dataGenerationFinished.release();
		}

	}
	
	
	protected void handleBCMessage(byte[] body) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            NodeMetadata[] nodeMetadata = (NodeMetadata[]) ois.readObject();
            domainNames = new String[nodeMetadata.length];
            for (int i = 0; i < nodeMetadata.length; ++i) {
                domainNames[i] = nodeMetadata[i].getHostname();
            }
            resource = new SparqlResource(domainId, domainNames,
	                graphs.toArray(new Graph[graphs.size()]), (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
	                        && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)), new String[] {},sink);
            resource.storeGraphs(graphs,sparqlContainer);
//            addDataSources(domainNames);
        } catch (Exception e) {
            LOGGER.error("Couldn't parse node metadata received from benchmark controller.", e);
            domainNames = null;
            throw new IllegalStateException("Didn't received the domain names from the benchmark controller.");
        }
        // In any case, we should release the semaphore. Otherwise, this component would
        // get stuck and wait forever for an additional message.
        domainNamesReceived.release();
    }
	
	@Override
	public void run() throws Exception {
		synchronized (this) {
			this.wait();
		}
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
