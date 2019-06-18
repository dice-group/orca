package org.dice_research.ldcbench.nodes.ckan.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.nodes.ckan.Constants;
import org.dice_research.ldcbench.nodes.ckan.dao.CkanDAO;
import org.dice_research.ldcbench.nodes.ckan.dao.PostgresCkanDAO;
import org.dice_research.ldcbench.nodes.rabbit.GraphHandler;
import org.hobbit.core.Commands;
import static org.hobbit.core.Constants.CONTAINER_TYPE_BENCHMARK;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.DataReceiver;
import org.hobbit.core.rabbit.DataReceiverImpl;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.trentorise.opendata.jackan.CheckedCkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanDatasetBase;
import eu.trentorise.opendata.jackan.model.CkanOrganization;

/**
 *
 * Ckan Node
 *
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleCkanComponent extends AbstractCommandReceivingComponent implements Component {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCkanComponent.class);

    private boolean dockerized;
    protected int cloudNodeId;
    protected String uriTemplate;

	protected Semaphore dataGenerationFinished = new Semaphore(0);

	protected Channel bcBroadcastChannel;
    protected DataReceiver receiver;
	protected static ConnectionFactory connectionFactory;

	protected String postGresContainer = null;
	protected String solrContainer = null;
	protected String redisContainer = null;
	protected String ckanContainer = null;
	protected String domainNames[];

    protected NodeMetadata[] nodeMetadata;

	private CkanDAO ckanDao;
	private List<CkanDataset> ckanDataSets = new ArrayList<CkanDataset>();


	public static void main(String[] args) {

//	    new PostgresCkanDAO("localhost").insertData();

		CkanDAO ckanDao = new CkanDAO(new CheckedCkanClient("http://localhost:80", Constants.CKAN_CLIENT_TOKEN));

		CkanDatasetBase ds = new CkanDatasetBase();
		ds.setName("dataset-test");
		ds.setTitle("dataset-test");
		ds.setOwnerOrg(Constants.ORGANIZATION);

//		ckanDao.insertDataSource(ds);
		ckanDao.deleteDataSource("dataset-test");

	}



	@Override
	public void init() throws Exception {
		super.init();

        dockerized = EnvVariables.getBoolean(ApiConstants.ENV_DOCKERIZED_KEY, true, LOGGER);
        cloudNodeId = EnvVariables.getInt(ApiConstants.ENV_NODE_ID_KEY, LOGGER);

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

//        // initialize graph queue
//        queueName = EnvVariables.getString(ApiConstants.ENV_DATA_QUEUE_KEY);
//        GraphHandler graphHandler = new GraphHandler();
//        receiver = DataReceiverImpl.builder().dataHandler(graphHandler).queue(this.incomingDataQueueFactory, queueName)
//                .build();
//
//        receiver.closeWhenFinished();

		LOGGER.warn("-- > Initializing Ckan Containers");

		postGresContainer = createContainer(Constants.POSTGRES, CONTAINER_TYPE_BENCHMARK, new String[] { "POSTGRES_PASSWORD=ckan",
				"POSTGRES_USER=ckan", "PGDATA=/var/postgresql/data", "POSTGRES_DB=ckan"
				});

        // FIXME
        LOGGER.info("Waiting to allow Postgres to initialize...");
        Thread.sleep(60000);

		solrContainer = createContainer(Constants.SOLR, CONTAINER_TYPE_BENCHMARK, null);
		redisContainer = createContainer(Constants.REDIS, CONTAINER_TYPE_BENCHMARK, null);
		ckanContainer = createContainer(Constants.CKAN, CONTAINER_TYPE_BENCHMARK,
				new String[] { "CKAN_SOLR_URL=http://" + solrContainer + ":8983/solr/ckan",
						"CKAN_SQLALCHEMY_URL=postgresql://ckan:ckan@" + postGresContainer + ":5432/ckan",
						"CKAN_REDIS_URL=redis://" + redisContainer + ":6379/0", "CKAN_SITE_URL=http://localhost",
						"CKAN_SITE_TITLE=CKAN NODE", "CKAN_SITE_DESCRIPTION=LDCBench Benchmark node",
						"CKAN_RECAPTCHA_PUBLICKEY=" + Constants.CKAN_RECAPTCHA_PUBLICKEY,
						"CKAN_RECAPTCHA_PRIVATEKEY=" + Constants.CKAN_RECAPTCHA_PRIVATEKEY,
						"REDIS_HOSTNAME=" + redisContainer,
                        "HOBBIT_SDK_PUBLISH_PORTS=5000",
        });

        // FIXME
        LOGGER.info("Waiting to allow CKAN to initialize...");
        Thread.sleep(60000);

        uriTemplate = "http://" + (dockerized ? ckanContainer : "localhost") + ":5000/";
        CheckedCkanClient client = new CheckedCkanClient(uriTemplate, Constants.CKAN_CLIENT_TOKEN);
		ckanDao = new CkanDAO(client);

//		CkanOrganization organization = new CkanOrganization();
//		organization.setName(Constants.ORGANIZATION);
//		ckanDao.insertOrganization(organization);

        sendToCmdQueue(ApiConstants.NODE_URI_TEMPLATE, RabbitMQUtils.writeByteArrays(new byte[][] {
                RabbitMQUtils.writeString(Integer.toString(cloudNodeId)), RabbitMQUtils.writeString(uriTemplate), RabbitMQUtils.writeString(uriTemplate), }));

        // Inform the BC that this node is ready
        sendToCmdQueue(ApiConstants.NODE_INIT_SIGNAL);

        // Wait for the data generation to finish
        dataGenerationFinished.acquire();

        sendToCmdQueue(ApiConstants.NODE_READY_SIGNAL);
    }

    private void addDataSource(String uri) {
        LOGGER.info("Adding {} to CKAN...", uri);
        CkanDatasetBase dataset = new CkanDatasetBase();
        dataset.setTitle(uri);
        dataset.setName(uri.replaceAll("[^A-Za-z0-9_-]", "_"));
        dataset.setOwnerOrg(Constants.ORGANIZATION);
        dataset.setAuthor(Constants.AUTHOR);
        ckanDataSets.add(ckanDao.insertDataSource(dataset));
    }

    protected void handleBCMessage(byte[] body) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            nodeMetadata = (NodeMetadata[]) ois.readObject();
            for (NodeMetadata nm : nodeMetadata) {
                addDataSource(new URI(String.format(nm.getAccessUriTemplate(), "", "", "", "")).toString());
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't parse node metadata received from benchmark controller.", e);
            nodeMetadata = null;
            throw new IllegalStateException("Didn't received the domain names from the benchmark controller.");
        }
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

		//delete all the datasets

		for(CkanDataset dataset: ckanDataSets) {
			ckanDao.deleteDataSource(dataset.getName());
		}

		super.close();
	}

}
