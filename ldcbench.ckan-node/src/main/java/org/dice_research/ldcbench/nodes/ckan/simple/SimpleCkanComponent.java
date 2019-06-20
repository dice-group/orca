package org.dice_research.ldcbench.nodes.ckan.simple;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.dice_research.ldcbench.data.NodeMetadata;
import org.dice_research.ldcbench.nodes.ckan.Constants;
import org.dice_research.ldcbench.nodes.ckan.dao.CkanDAO;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import static org.hobbit.core.Constants.CONTAINER_TYPE_BENCHMARK;
import org.hobbit.core.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CheckedCkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanDatasetBase;

/**
 *
 * Ckan Node
 *
 * @author Geraldo de Souza Junior
 *
 */

public class SimpleCkanComponent extends AbstractNodeComponent implements Component {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCkanComponent.class);

	protected String postGresContainer = null;
	protected String solrContainer = null;
	protected String redisContainer = null;
	protected String ckanContainer = null;

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
    public void initBeforeDataGeneration() throws Exception {
		postGresContainer = createContainer(Constants.POSTGRES, CONTAINER_TYPE_BENCHMARK, new String[] { "POSTGRES_PASSWORD=ckan",
				"POSTGRES_USER=ckan", "PGDATA=/var/postgresql/data", "POSTGRES_DB=ckan"
				});

        // FIXME
        LOGGER.info("Waiting to allow Postgres to initialize...");
        Thread.sleep(60000);

		solrContainer = createContainer(Constants.SOLR, CONTAINER_TYPE_BENCHMARK, null);
		redisContainer = createContainer(Constants.REDIS, CONTAINER_TYPE_BENCHMARK, null);

        LOGGER.debug("Starting CKAN service: {}...", Constants.CKAN);
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

        accessUriTemplate = "http://" + (dockerized ? ckanContainer : "localhost") + ":5000/";
        resourceUriTemplate = accessUriTemplate;
        CheckedCkanClient client = new CheckedCkanClient(accessUriTemplate, Constants.CKAN_CLIENT_TOKEN);
		ckanDao = new CkanDAO(client);
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

    @Override
    public void initAfterDataGeneration() throws Exception {
        for (NodeMetadata nm : nodeMetadata) {
            addDataSource(new URI(String.format(nm.getAccessUriTemplate(), "", "", "", "")).toString());
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

        IOUtils.closeQuietly(receiver);
        if (bcBroadcastConsumer != null) {
            //bcBroadcastConsumer.close();
        }

		super.close();
	}

}
