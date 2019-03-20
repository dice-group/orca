package org.dice_research.ldcbench.nodes.ckan.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.ckan.Constants;
import org.dice_research.ldcbench.nodes.rabbit.GraphHandler;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.DataReceiver;
import org.hobbit.core.rabbit.DataReceiverImpl;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 
 * Ckan Node
 * 
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleCkanComponent extends AbstractCommandReceivingComponent implements Component {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCkanComponent.class);

	protected Semaphore dataGenerationFinished = new Semaphore(0);
	protected Semaphore domainNamesReceived = new Semaphore(0);

	protected Channel bcBroadcastChannel;
    protected DataReceiver receiver;
	protected static ConnectionFactory connectionFactory;

	protected String postGresContainer = null;
	protected String solrContainer = null;
	protected String redisContainer = null;
	protected String ckanContainer = null;
	protected String domainNames[];

	@Override
	public void init() throws Exception {
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

        // initialize graph queue
        queueName = EnvVariables.getString(ApiConstants.ENV_DATA_QUEUE_KEY);
        GraphHandler graphHandler = new GraphHandler();
        receiver = DataReceiverImpl.builder().dataHandler(graphHandler).queue(this.incomingDataQueueFactory, queueName)
                .build();

        // Wait for the data generation to finish
        dataGenerationFinished.acquire();
        receiver.closeWhenFinished();

        if (graphHandler.encounteredError()) {
            throw new IllegalStateException("Encountered an error while receiving graphs.");
        }
        List<Graph> graphs = graphHandler.getGraphs();
        if (graphs.isEmpty()) {
            throw new IllegalStateException("Didn't received a single graph.");
        }
        if (domainNames == null) {
            throw new IllegalStateException("Didn't received the domain names from the benchmark controller.");
        }

		LOGGER.warn("-- > Initializing Ckan Containers");

		postGresContainer = createContainer(Constants.POSTGRES, new String[] { "POSTGRES_PASSWORD=ckan",
				"POSTGRES_USER=ckan", "PGDATA=/var/lib/postgresql/data", "POSTGRES_DB=ckan" });

		solrContainer = createContainer(Constants.SOLR, null);
		redisContainer = createContainer(Constants.REDIS, null);
		ckanContainer = createContainer(Constants.CKAN,
				new String[] { "CKAN_SOLR_URL=http://" + solrContainer + ":8983/solr/ckan",
						"CKAN_SQLALCHEMY_URL=postgresql://ckan:ckan@" + postGresContainer + ":5432/ckan",
						"CKAN_REDIS_URL=redis://" + redisContainer + ":6379/0", "CKAN_SITE_URL=http://localhost",
						"CKAN_SITE_TITLE=CKAN NODE", "CKAN_SITE_DESCRIPTION=LDCBench Benchmark node",
						"CKAN_RECAPTCHA_PUBLICKEY=" + Constants.CKAN_RECAPTCHA_PUBLICKEY,
						"CKAN_RECAPTCHA_PRIVATEKEY=" + Constants.CKAN_RECAPTCHA_PRIVATEKEY,
						"REDIS_HOSTNAME=" + redisContainer });

		postGresContainer = createContainer(Constants.CKAN,
				new String[] { "CKAN_SQLALCHEMY_URL=postgresql://ckan:ckan@" + postGresContainer + ":5432/ckan" });

		LOGGER.warn("-- > Ckan Containers Initialized");

//		ckanDAO = new CkanDAO(new CheckedCkanClient("http://localhost:80", TOKEN));

	}
	
	
	 protected void handleBCMessage(byte[] body) {
	        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
	            NodeMetadata[] nodeMetadata = (NodeMetadata[]) ois.readObject();
	            domainNames = new String[nodeMetadata.length];
	            for (int i = 0; i < nodeMetadata.length; ++i) {
	                domainNames[i] = nodeMetadata[i].getHostname();
	            }
	        } catch (Exception e) {
	            LOGGER.error("Couldn't parse node metadata received from benchmark controller.", e);
	            domainNames = null;
	        }
	        // In any case, we should release the semaphore. Otherwise, this component would
	        // get stuck and wait forever for an additional message.
	        domainNamesReceived.release();
	    }

	@Override
	public void receiveCommand(byte command, byte[] data) {
		switch (command) {
		case Commands.DATA_GENERATION_FINISHED:
			LOGGER.debug("Received DATA_GENERATION_FINISHED");
			dataGenerationFinished.release();
		}

	}

	@Override
	public void run() throws Exception {
		synchronized (this) {
			this.wait();
		}
	}

	@Override
	public void close() throws IOException {

		if (postGresContainer != null) {
			LOGGER.debug("Stopping Postgres {}", postGresContainer);
			stopContainer(postGresContainer);
		} else {
			LOGGER.debug("There is no Postgres to stop.");
		}

		if (solrContainer != null) {
			LOGGER.debug("Stopping Solr {}", solrContainer);
			stopContainer(solrContainer);
		} else {
			LOGGER.debug("There is no Solr to stop.");
		}

		if (redisContainer != null) {
			LOGGER.debug("Stopping Redis {}", redisContainer);
			stopContainer(redisContainer);
		} else {
			LOGGER.debug("There is no Redis to stop.");
		}

		if (ckanContainer != null) {
			LOGGER.debug("Stopping Ckan {}", ckanContainer);
			stopContainer(ckanContainer);
		} else {
			LOGGER.debug("There is no Ckan to stop.");
		}

		super.close();
	}

}
