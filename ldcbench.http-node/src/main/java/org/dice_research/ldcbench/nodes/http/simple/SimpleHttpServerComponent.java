package org.dice_research.ldcbench.nodes.http.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.hobbit.core.components.Component;
import org.hobbit.utils.EnvVariables;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpServerComponent extends AbstractNodeComponent implements Component {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpServerComponent.class);

    private static final int DEFAULT_PORT = 80;

    protected Container container;
    protected Server server;
    protected Connection connection;

    @Override
    public void initBeforeDataGeneration() throws Exception {}

    @Override
    public void initAfterDataGeneration() throws Exception {
        // Create the container based on the information that has been received
        container = new CrawleableResourceContainer(new GraphBasedResource(cloudNodeId, uriTemplates,
                graphs.toArray(new Graph[graphs.size()]), (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
                        && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)),
                new String[] {
                // "application/rdf+xml", "text/plain", "*/*"
                }));
        graphs = null;
        // Start server
        server = new ContainerServer(container);
        connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(
                EnvVariables.getInt(ApiConstants.ENV_HTTP_PORT_KEY, DEFAULT_PORT, LOGGER));
        connection.connect(address);
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
    public void close() throws IOException {
        IOUtils.closeQuietly(connection);
        try {
            if (server != null) {
                server.stop();
            }
        } catch (IOException e) {
            LOGGER.error("Exception while closing server. It will be ignored.", e);
        }
        super.close();
    }
}
