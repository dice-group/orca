package org.dice_research.ldcbench.nodes.http.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_research.ldcbench.ApiConstants;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.components.AbstractNodeComponent;
import org.dice_research.ldcbench.nodes.http.simple.dump.DumpFileResource;
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
    protected boolean dumpFileNode;

    @Override
    public void initBeforeDataGeneration() throws Exception {
        // check whether this node contains dump files
        dumpFileNode = EnvVariables.getBoolean("LDCBENCH_USE_DUMP_FILE", false);
        if (dumpFileNode) {
            LOGGER.debug("Init as HTTP dump file node.");
            this.resourceUriTemplate = "http://" + myself + "/dumpFile.ttl#%s-%s/%s-%s";
            this.accessUriTemplate = "http://" + myself + "/dumpFile.ttl#%s-%s/%s-%s";
        } else {
            LOGGER.debug("Init as dereferencing HTTP node.");
        }
    }

    @Override
    public void initAfterDataGeneration() throws Exception {
        // Create the container based on the information that has been received
        container = createContainer();
        graphs = null;
        // Start server
        server = new ContainerServer(container);
        connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(
                EnvVariables.getInt(ApiConstants.ENV_HTTP_PORT_KEY, DEFAULT_PORT, LOGGER));
        connection.connect(address);
    }

    protected Container createContainer() {
        CrawleableResource resource = null;
        if (dumpFileNode) {
            resource = DumpFileResource.create(cloudNodeId,
                    Stream.of(nodeMetadata).map(nm -> nm.getResourceUriTemplate()).toArray(String[]::new),
                    Stream.of(nodeMetadata).map(nm -> nm.getAccessUriTemplate()).toArray(String[]::new),
                    graphs.toArray(new Graph[graphs.size()]), (r -> true), new String[] {});
        } else {
            // Create the container based on the information that has been received
            resource = new GraphBasedResource(cloudNodeId,
                    Stream.of(nodeMetadata).map(nm -> nm.getResourceUriTemplate()).toArray(String[]::new),
                    Stream.of(nodeMetadata).map(nm -> nm.getAccessUriTemplate()).toArray(String[]::new),
                    graphs.toArray(new Graph[graphs.size()]), (r -> r.getTarget().contains(UriHelper.DATASET_KEY_WORD)
                            && r.getTarget().contains(UriHelper.RESOURCE_NODE_TYPE)),
                    new String[] {
                    // "application/rdf+xml", "text/plain", "*/*"
                    });
        }
        Objects.requireNonNull(resource, "Couldn't create crawleable resource. Exiting.");
        return new CrawleableResourceContainer(resource);
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
