package org.dice_research.ldcbench.system;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

public class SystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");

        //Getting default values from system.ttl
        Property parameter;
        NodeIterator objIterator;
        ResIterator iterator = systemParamModel.listResourcesWithProperty(RDF.type, HOBBIT.Parameter);
        Property defaultValProperty = systemParamModel.getProperty("http://w3id.org/hobbit/vocab#defaultValue");
        while (iterator.hasNext()) {
            parameter = systemParamModel.getProperty(iterator.next().getURI());
            objIterator = systemParamModel.listObjectsOfProperty(parameter, defaultValProperty);
            while (objIterator.hasNext()) {
                String value = objIterator.next().asLiteral().getString();
                parameters.put(parameter.getLocalName(), value);
            }
        }
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        String sparqlUrl = RabbitMQUtils.readString(buffer);
        String sparqlUser = RabbitMQUtils.readString(buffer);
        String sparqlPwd = RabbitMQUtils.readString(buffer);
        ArrayList<String> seedURIs = new ArrayList<>(Arrays.asList(RabbitMQUtils.readString(buffer).split("\n")));

        logger.info("SPARQL endpoint: " + sparqlUrl);
        assert sparqlUrl.length() > 0;
        logger.info("Seed URIs: {}.", seedURIs);
        assert seedURIs.size() > 0;
        logger.info("SPARQL endpoint username: {}.", sparqlUser);
        assert sparqlUser.length() > 0;
        assert sparqlPwd.length() > 0;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sparqlUser, sparqlPwd));
        HttpClient httpClient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        Node graph = NodeFactory.createURI("http://localhost:8890/sparql");

        while (seedURIs.size() != 0) {
            String uri = seedURIs.remove(0);

            try {
                if (uri.matches(".*:5000/")) {
                    logger.info("Accessing CKAN {}...", uri);
                    try (InputStream listStream = new URL(uri + "api/3/action/package_list").openStream()) {
                        JsonReader jr = Json.createReader(listStream);
                        for (JsonValue id : jr.readObject().getJsonArray("result")) {
                            try (InputStream showStream = new URL(uri + "api/3/action/package_show?id=" + ((JsonString)id).getString()).openStream()) {
                                String resourceUrl = Json.createReader(showStream).readObject().getJsonObject("result").getJsonArray("resources").getJsonObject(0).getString("url");
                                logger.info("CKAN resource: {}", resourceUrl);
                                seedURIs.add(resourceUrl);
                            }
                        }
                    }
                } else {
                    logger.info("Crawling {}...", uri);
                    URL url = new URL(uri);

                    Integer crawlDelay = null;
                    try (InputStream stream = new URL(url, "/robots.txt").openStream()) {
                        String robots = IOUtils.toString(stream, Charset.defaultCharset());
                        crawlDelay = Stream.of(robots.split("\n"))
                                .filter(s -> s.startsWith("Crawl-delay: "))
                                .findFirst()
                                .map(s -> Integer.parseInt(s.split(": ")[1]))
                                .orElse(null);
                    } catch (Exception e) {
                        logger.error("Exception while trying to access robots.txt.", e);
                    }

                    Model model = ModelFactory.createDefaultModel();
                    if (url.getPath().endsWith(".ttl.gz")) {
                        model.read(new GZIPInputStream(url.openStream()), null, "TURTLE");
                    } else {
                        model.read(uri);
                    }
                    logger.info("Model from {}: {}", uri, model.toString());

                    UpdateExecutionFactory.createRemoteForm(
                        new UpdateRequest(new UpdateDataInsert(new QuadDataAcc(
                            model.listStatements().toList().stream()
                            .map(stmt -> stmt.asTriple())
                            .map(tri -> new Quad(graph, tri))
                            .collect(Collectors.toList())
                        ))),
                        sparqlUrl,
                        httpClient
                    ).execute();

                    if (crawlDelay != null) {
                        logger.info("Crawl-delay is {}, will crawl again...", crawlDelay);
                        Thread.sleep(crawlDelay * 1000);
                        model.read(url.openStream(), null, "TURTLE");
                    }

                    logger.info("Crawled {}.", uri);
                }
            } catch (Exception e) {
                logger.error("Failed to crawl {}.", uri, e);
            }
        }

        // try {
        //     Thread.sleep(6000000);
        // } catch (InterruptedException e) {
        // }

        logger.info("Dummy system terminates.");
        terminate(null);
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        throw new IllegalStateException("Should not receive any tasks.");
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }
}
