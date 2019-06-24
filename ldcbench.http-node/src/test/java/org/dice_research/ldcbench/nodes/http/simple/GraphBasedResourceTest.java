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

    private static final String[] URI_TEMPLATES = new String[] { "http://domain0.org/%s-%s/%s-%s", "http://domain1.org/%s-%s/%s-%s" };

    @Test
    public void testTurtle1() throws Exception {
        executeTest1(Lang.TURTLE);
    }

    @Test
    public void testRDFXML1() throws Exception {
        executeTest1(Lang.RDFXML);
    }

    @Test
    public void testJSONLD1() throws Exception {
        executeTest1(Lang.JSONLD);
    }

    protected void executeTest1(Lang lang) throws Exception {
        GraphBuilder builder = new GrphBasedGraph();
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
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0, 0),
                getResource(expectedModel, 1, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 1, 0),
                getResource(expectedModel, 1, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0, 0),
                getResource(expectedModel, 2, 0));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0, 0),
                getResource(expectedModel, 0, 1));
        expectedModel.add(getResource(expectedModel, 0, 0), getProperty(expectedModel, 0, 0),
                getResource(expectedModel, 1, 1));

        executeTest(lang, builder, 0, expectedModel);
    }

    @Test
    public void testTurtle2() throws Exception {
        executeTest2(Lang.TURTLE);
    }

    @Test
    public void testRDFXML2() throws Exception {
        executeTest2(Lang.RDFXML);
    }

    @Test
    public void testJSONLD2() throws Exception {
        executeTest2(Lang.JSONLD);
    }

    protected void executeTest2(Lang lang) throws Exception {
        GraphBuilder builder = new GrphBasedGraph();
        // build graph
        builder.addNodes(5);
        builder.setGraphIdOfNode(3, 0, 0);
        builder.setGraphIdOfNode(4, 0, 1);
        builder.addEdge(0, 1, 0);
        builder.addEdge(0, 1, 1);
        builder.addEdge(0, 2, 0);
        builder.addEdge(0, 3, 0);
        builder.addEdge(0, 4, 0);
        // Create expected model
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.add(getResource(expectedModel, 0, 1), getProperty(expectedModel, 0, 1),
                getResource(expectedModel, 1, 1));
        expectedModel.add(getResource(expectedModel, 0, 1), getProperty(expectedModel, 1, 1),
                getResource(expectedModel, 1, 1));
        expectedModel.add(getResource(expectedModel, 0, 1), getProperty(expectedModel, 0, 1),
                getResource(expectedModel, 2, 1));
        expectedModel.add(getResource(expectedModel, 0, 1), getProperty(expectedModel, 0, 1),
                getResource(expectedModel, 0, 0));
        expectedModel.add(getResource(expectedModel, 0, 1), getProperty(expectedModel, 0, 1),
                getResource(expectedModel, 1, 0));

        executeTest(lang, builder, 1, expectedModel);
    }

    protected void executeTest(Lang lang, Graph graph, int domainId, Model expectedModel) throws Exception {
        Graph[] graphs = new Graph[] { graph };

        // create resource
        GraphBasedResource resource = new GraphBasedResource(domainId, URI_TEMPLATES, URI_TEMPLATES, graphs, (r -> true), new String[0]);

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
        Set<Statement> wrongStmts = ModelComparisonHelper.getMissingStatements(expectedModel, receivedModel);
        if (!wrongStmts.isEmpty()) {
            System.out.println("Wrong statments: ");
            System.out.println(wrongStmts.toString());
        }
        Assert.assertTrue("There were missing or wrong statements.", missingStmts.isEmpty() && wrongStmts.isEmpty());
    }

    private Property getProperty(Model model, int id, int domainId) {
        return model.getProperty(getUri("property", id, domainId));
    }

    private Resource getResource(Model model, int nodeId, int domainId) {
        return model.getResource(getUri("resource", nodeId, domainId));
    }

    private String getUri(String nodeType, int nodeId, int domainId) {
        return String.format(URI_TEMPLATES[domainId], "dataset", 0, nodeType, nodeId);
    }
}
