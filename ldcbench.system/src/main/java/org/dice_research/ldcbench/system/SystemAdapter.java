package org.dice_research.ldcbench.system;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import org.apache.any23.Any23;
import org.apache.any23.source.StringDocumentSource;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandler;
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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.jena.riot.RDFLanguages.TURTLE;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    Set<String> seedURIs;
    Set<String> queuedURIs;
    Set<String> processedURIs = new HashSet<>();

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
        seedURIs = new HashSet<>(Arrays.asList(RabbitMQUtils.readString(buffer).split("\n")));
        queuedURIs = new HashSet<>(seedURIs);

        logger.debug("SPARQL endpoint: " + sparqlUrl);
        assert sparqlUrl.length() > 0;
        logger.debug("SPARQL endpoint username: {}", sparqlUser);
        assert sparqlUser.length() > 0;
        assert sparqlPwd.length() > 0;
        logger.info("Seed URIs: {}", seedURIs);
        assert seedURIs.size() > 0;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sparqlUser, sparqlPwd));
        HttpClient httpClient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        Node graph = NodeFactory.createURI("http://localhost:8890/sparql");

        logger.info("Crawling starts.");

        while (queuedURIs.size() != 0) {
            String uri = queuedURIs.iterator().next();
            queuedURIs.remove(uri);
            processedURIs.add(uri);

            try {
                logger.debug("URI: {}", uri);

                if (uri.matches(".*:5000/")) {
                    logger.debug("Using CKAN API...");
                    try (InputStream listStream = new URL(uri + "api/3/action/package_list").openStream()) {
                        JsonReader jr = Json.createReader(listStream);
                        for (JsonValue id : jr.readObject().getJsonArray("result")) {
                            try (InputStream showStream = new URL(uri + "api/3/action/package_show?id=" + ((JsonString)id).getString()).openStream()) {
                                String resourceUrl = Json.createReader(showStream).readObject().getJsonObject("result").getJsonArray("resources").getJsonObject(0).getString("url");
                                logger.debug("Got CKAN resource: {}", resourceUrl);
                                queueURI(resourceUrl);
                            }
                        }
                    }
                } else {
                    URL url = new URL(uri);

                    Integer crawlDelay = null;
                    try (InputStream stream = new URL(url, "/robots.txt").openStream()) {
                        String robots = IOUtils.toString(stream, Charset.defaultCharset());
                        crawlDelay = Stream.of(robots.split("\n"))
                                .filter(s -> s.startsWith("Crawl-delay: "))
                                .findFirst()
                                .map(s -> Integer.parseInt(s.split(": ")[1]))
                                .orElse(null);
                        if (crawlDelay != null) {
                            logger.debug("Crawl-delay is {}, waiting between accessing robots.txt and the needed URL...", crawlDelay);
                            Thread.sleep(crawlDelay * 1000);
                        }
                    } catch (FileNotFoundException e) {
                        // no robots.txt
                    } catch (Exception e) {
                        logger.error("Exception while trying to access robots.txt.", e);
                    }

                    Model model = ModelFactory.createDefaultModel();
                    URLConnection con = url.openConnection();
                    InputStream input = con.getInputStream();
                    String path = url.getPath();
                    {
                        Matcher m = Pattern.compile("^(.+)\\.(gz)$").matcher(path);
                        if (m.find()) {
                            path = m.group(1);
                            if (m.group(2).equals("gz")) {
                                input = new GZIPInputStream(input);
                            }
                        }
                    }

                    String contentType = con.getContentType();
                    logger.debug("Content-Type: {}", contentType);

                    if ("text/html".equals(contentType)) {
                        logger.debug("Using Any23 to extract embedded data...");
                        Any23 runner = new Any23();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        try (TripleHandler handler = new NTriplesWriter(out)) {
                            runner.extract(new StringDocumentSource(IOUtils.toString(input, con.getContentEncoding()), uri), handler);
                        }
                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                        model.read(in, null, RDFLanguages.NTRIPLES.getName());
                    } else {
                        Matcher m = Pattern.compile("^(.+)\\.([^.]+)$").matcher(path);
                        Lang lang = m.find() ? RDFLanguages.fileExtToLang(m.group(2)) : TURTLE;
                        logger.debug("Using Jena to read data as {}...", lang);
                        model.read(input, null, lang.getName());
                    }

                    logger.debug("Model size: {}", model.size());

                    if (path.matches("/dataset-\\d+/resource-\\d+")) {
                        model.listObjects().forEachRemaining(this::queueURI);
                    }

                    UpdateDeleteInsert update = new UpdateDeleteInsert();
                    QuadAcc insertQuads = update.getInsertAcc();
                    model.listStatements().toList().stream()
                    .map(Statement::asTriple)
                    .map(triple -> new Quad(graph, triple))
                    .forEach(insertQuads::addQuad);

                    UpdateExecutionFactory.createRemoteForm(
                        new UpdateRequest(update),
                        sparqlUrl,
                        httpClient
                    ).execute();

                    if (crawlDelay != null) {
                        // FIXME: Only works correctly with dereferencing nodes,
                        // but at the moment only these nodes have crawlDelay
                        logger.debug("Crawl-delay is {}, will crawl again...", crawlDelay);
                        Thread.sleep(crawlDelay * 1000);
                        model.read(url.openStream(), null, RDFLanguages.TTL.getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to process {}", uri, e);
            }

            logger.info("Crawling: {} / {}", processedURIs.size(), queuedURIs.size() + processedURIs.size());
        }

        logger.info("Crawling finished.");

        // try {
        //     Thread.sleep(6000000);
        // } catch (InterruptedException e) {
        // }

        logger.info("System adapter will terminate.");
        terminate(null);
    }

    void queueURI(String uri) {
        if (!processedURIs.contains(uri)) {
            queuedURIs.add(uri);
        }
    }

    void queueURI(RDFNode node) {
        if (node instanceof Resource) {
            queueURI(((Resource)node).getURI());
        }
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
