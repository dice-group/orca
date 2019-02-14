package org.dice_research.ldcbench.nodes.http.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.DataHandler;
import org.hobbit.core.rabbit.DataReceiver;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpServerComponent /* extends AbstractComponent */ implements Component, DataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpServerComponent.class);

    protected Container container;
    protected Server server;
    protected Connection connection;
    protected String graphNames[];
    protected DataReceiver receiver;

    @Override
    public void init() throws Exception {
        // super.init();
        // TODO initialize exchange with BC
        // TODO initialize graph queue
        // String queueName = EnvVariables.getString("TODO PLEASE ADD KEY NAME FOR QUEUE
        // NAMES!!!");
        // receiver =
        // DataReceiverImpl.builder().dataHandler(this).queue(this.incomingDataQueueFactory,
        // queueName).build();

        Graph graph;
        // XXX FOR DEBUGGING (remove me)
        GraphBuilder builder = new GrphBasedGraph();
        int nodeIds[] = builder.addNodes(2);
        builder.addEdge(nodeIds[0], nodeIds[0] + 1, 0);
        graph = builder;
        // XXX END DEBUGGING
        if (graph == null) {
            throw new IllegalArgumentException("Couldn't read graph.");
        }

        // container = new CrawleableResourceContainer(resources.toArray(new
        // CrawleableResource[resources.size()]));
        container = new CrawleableResourceContainer(new GraphBasedResource(0, new String[] { "example.org" },
                new Graph[] { graph }, (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
                        && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)),
                new String[] {
                // "application/rdf+xml", "text/plain", "*/*"
                }));
        server = new ContainerServer(container);
        connection = new SocketConnection(server);
        // TODO make port configurable
        SocketAddress address = new InetSocketAddress(8080);
        connection.connect(address);

        LOGGER.info("HTTP server initialized.");
    }

    protected Model readModel(String modelFile, String modelLang) {
        Model model = ModelFactory.createDefaultModel();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(modelFile);
            model.read(fin, "", modelLang);
        } catch (Exception e) {
            LOGGER.error("Couldn't read model file. Returning null.", e);
            return null;
        } finally {
            IOUtils.closeQuietly(fin);
        }
        return model;
    }

    @Override
    public void run() throws Exception {
        synchronized (this) {
            this.wait();
        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(connection);
        try {
            if (server != null) {
                server.stop();
            }
        } catch (IOException e) {
            LOGGER.error("Exception while closing server. It will be ignored.", e);
        }
        IOUtils.closeQuietly(receiver);
        // super.close();
    }

    @Override
    public void handleData(byte[] data) {
        // TODO parse incoming graph

    }

}
