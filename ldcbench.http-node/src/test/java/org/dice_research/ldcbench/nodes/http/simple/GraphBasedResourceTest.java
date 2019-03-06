package org.dice_research.ldcbench.nodes.http.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.hobbit.utils.test.ModelComparisonHelper;
import org.junit.Test;

import junit.framework.Assert;

public class GraphBasedResourceTest {

    private static final String[] DOMAINS = new String[] { "domain0", "domain1" };

    @Test
    public void testTurtle() throws Exception {
        executeTest(Lang.TURTLE);
    }

    @Test
    public void testRDFXML() throws Exception {
        executeTest(Lang.RDFXML);
    }

    @Test
    public void testJSONLD() throws Exception {
        executeTest(Lang.JSONLD);
    }

    public void executeTest(Lang lang) throws Exception {
        int domainId = 0;
        GraphBuilder builder = new GrphBasedGraph();
        Graph[] graphs = new Graph[] { builder };

        // build graph
        builder.addNodes(5);
        builder.setGraphIdOfNode(3, 1, 0);
        builder.setGraphIdOfNode(4, 1, 1);
        builder.addEdge(0, 1, 0);
        builder.addEdge(0, 1, 1);
        builder.addEdge(0, 2, 0);
        builder.addEdge(0, 3, 0);
        builder.addEdge(0, 4, 0);

        // Create expected model
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0),
                getResource(expectedModel, 1, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 1),
                getResource(expectedModel, 1, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0),
                getResource(expectedModel, 2, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0),
                getResource(expectedModel, 0, 1));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0),
                getResource(expectedModel, 1, 1));

        // create resource
        GraphBasedResource resource = new GraphBasedResource(domainId, DOMAINS, graphs, (r -> true), new String[0]);

        // request data from resource
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resource.handleRequest(getUri("resource", 0, 0), lang, StandardCharsets.UTF_8.name(), out);

        // parse the received data
        Model receivedModel = ModelFactory.createDefaultModel();
        receivedModel.read(new ByteArrayInputStream(out.toByteArray()), "", lang.getName());

        // compare the data
        Set<Statement> missingStmts = ModelComparisonHelper.getMissingStatements(receivedModel, expectedModel);
        if (!missingStmts.isEmpty()) {
            System.out.println("Missing statments: ");
            System.out.println(missingStmts.toString());
        }
        Set<Statement> wrongStmts = ModelComparisonHelper.getMissingStatements(receivedModel, expectedModel);
        if (!wrongStmts.isEmpty()) {
            System.out.println("Wrong statments: ");
            System.out.println(wrongStmts.toString());
        }
        Assert.assertTrue("There were missing or wrong statements.", missingStmts.isEmpty() && wrongStmts.isEmpty());
    }

    private Property getProperty(Model model, int id) {
        return model.getProperty(getUri("property", id, 0));
    }

    private Resource getResource(Model model, int nodeId, int domainId) {
        return model.getResource(getUri("resource", nodeId, domainId));
    }

    private String getUri(String nodeType, int nodeId, int domainId) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://");
        builder.append(DOMAINS[domainId]);
        builder.append("/dataset-0/");
        builder.append(nodeType);
        builder.append("-");
        builder.append(nodeId);
        return builder.toString();
    }
}
