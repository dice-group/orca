package org.dice_research.ldcbench.benchmark.eval.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.benchmark.eval.ValidationResult;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.GraphBasedTripleBlockStreamCreator;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.TripleBlockStreamSupplier;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SparqlBasedValidatorTest implements TripleBlockStreamSupplier {

    protected Dataset dataset;
    protected String[] domains;
    protected Graph[] graphs;
    protected ValidationResult[] expectedResults;

    @Parameters
    public static List<Object[]> testCases() {
        List<Object[]> data = new ArrayList<>();

        // Graph 1: Simple graph containing a triangle of three nodes
        GraphBuilder builder1 = new GrphBasedGraph();
        builder1.addNodes(3);
        builder1.addEdge(0, 1, 0);
        builder1.addEdge(1, 2, 0);
        builder1.addEdge(2, 0, 0);
        builder1.setEntranceNodes(new int[] { 0, 1, 2 });

        // Graph 2: a graph with different edge types and an external node
        GraphBuilder builder2 = new GrphBasedGraph();
        builder2.addNodes(3);
        builder2.addEdge(0, 1, 0);
        builder2.addEdge(0, 1, 1);
        builder2.addEdge(0, 2, 2);
        builder2.setGraphIdOfNode(2, 0, 0);
        builder2.setEntranceNodes(new int[] { 0 });

        Graph[] graphs = new Graph[] { builder1, builder2 };
        String[] domains = new String[] { "http://domain0.org/%s-%s/%s-%s", "http://domain1.org/%s-%s/%s-%s" };

        // First graph is completely correct, second graph is empty
        Dataset dataset;
        Model model;
        dataset = DatasetFactory.createTxnMem();
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain0.org/dataset-0/resource-0"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-1"));
        model.add(model.getResource("http://domain0.org/dataset-0/resource-1"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-2"));
        model.add(model.getResource("http://domain0.org/dataset-0/resource-2"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-0"));
        dataset.addNamedModel("http://domain0.org", model);
        Model correctModel1 = model;
        model = ModelFactory.createDefaultModel();
        dataset.addNamedModel("http://domain1.org", model);
        data.add(new Object[] { dataset, domains, graphs,
                new ValidationResult[] { new ValidationResult(3, 3), new ValidationResult(3, 0) } });

        // First graph is empty, second graph is completely correct
        dataset = DatasetFactory.createTxnMem();
        model = ModelFactory.createDefaultModel();
        dataset.addNamedModel("http://domain0.org", model);
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
                model.getProperty("http://domain1.org/dataset-0/property-0"),
                model.getResource("http://domain1.org/dataset-0/resource-1"));
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
                model.getProperty("http://domain1.org/dataset-0/property-1"),
                model.getResource("http://domain1.org/dataset-0/resource-1"));
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
                model.getProperty("http://domain1.org/dataset-0/property-2"),
                model.getResource("http://domain0.org/dataset-0/resource-0"));
        dataset.addNamedModel("http://domain1.org", model);
        Model correctModel2 = model;
        data.add(new Object[] { dataset, domains, graphs,
                new ValidationResult[] { new ValidationResult(3, 0), new ValidationResult(3, 3) } });

        // First both are correct
        dataset = DatasetFactory.createTxnMem();
        dataset.addNamedModel("http://domain0.org", correctModel1);
        dataset.addNamedModel("http://domain1.org", correctModel2);
        data.add(new Object[] { dataset, domains, graphs,
                new ValidationResult[] { new ValidationResult(3, 3), new ValidationResult(3, 3) } });

        // First graph has two correct triples, second graph has only one correct triple
        // and one additional triple which shouldn't matter
        dataset = DatasetFactory.createTxnMem();
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain0.org/dataset-0/resource-0"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-1"));
        model.add(model.getResource("http://domain0.org/dataset-0/resource-2"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-0"));
        dataset.addNamedModel("http://domain0.org", model);
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain1.org/dataset-0/resource-YYY"),
                model.getProperty("http://domain1.org/dataset-0/property-0"),
                model.getResource("http://domain1.org/dataset-0/resource-1"));
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
                model.getProperty("http://domain1.org/dataset-0/property-2"),
                model.getResource("http://domain0.org/dataset-0/resource-0"));
        dataset.addNamedModel("http://domain1.org", model);
        data.add(new Object[] { dataset, domains, graphs,
                new ValidationResult[] { new ValidationResult(3, 2), new ValidationResult(3, 1) } });

        // Graph with blank Node and Literal

        // Graph 1: Simple graph containing blank nodes
        builder1 = new GrphBasedGraph();
        builder1.addNodes(3);
        builder1.setBlankNodesRange(1,2);
        builder1.addEdge(0, 0, 0);
        builder1.addEdge(0, 1, 0);
        builder1.addEdge(0, 2, 1);
        builder1.setEntranceNodes(new int[] { 0 });

        // Graph 2: Simple graph containing blank nodes
        builder2 = new GrphBasedGraph();
        builder2.addNodes(3);
        builder2.setLiteralsRange(1,2);
        builder2.addEdge(0, 0, 0);
        builder2.addEdge(0, 1, 0);
        builder2.addEdge(0, 2, 1);
        builder2.setEntranceNodes(new int[] { 0 });
        dataset = DatasetFactory.createTxnMem();
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain0.org/dataset-0/resource-0"),
                model.getProperty("http://domain0.org/dataset-0/property-1"),
                model.getRDFNode(NodeFactory.createBlankNode("2c1e21645df88fe91a7b0770fd5e06c6")));
        model.add(model.getResource("http://domain0.org/dataset-0/resource-0"),
                model.getProperty("http://domain0.org/dataset-0/property-0"),
                model.getResource("http://domain0.org/dataset-0/resource-0"));
        dataset.addNamedModel("http://domain0.org", model);
        model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
        model.getProperty("http://domain1.org/dataset-0/property-0"),
        model.getResource("http://domain1.org/dataset-0/resource-0"));
        model.add(model.getResource("http://domain1.org/dataset-0/resource-0"),
        model.getProperty("http://domain1.org/dataset-0/property-0"),
        model.getRDFNode(NodeFactory.createLiteral("literal-1")));
        dataset.addNamedModel("http://domain1.org", model);

        graphs = new Graph[] { builder1, builder2 };

        data.add(new Object[] { dataset, domains, graphs,
                new ValidationResult[] { new ValidationResult(3, 2), new ValidationResult(3, 2) } });

        return data;
    }

    public SparqlBasedValidatorTest(Dataset dataset, String[] domains, Graph[] graphs,
            ValidationResult[] expectedResults) {
        this.dataset = dataset;
        this.domains = domains;
        this.graphs = graphs;
        this.expectedResults = expectedResults;
    }

    @Test
    public void test() throws Exception {
        try (SparqlBasedValidator validator = new SparqlBasedValidator(new QueryExecutionFactoryDataset(dataset))) {
            ValidationResult result;
            for (int i = 0; i < expectedResults.length; ++i) {
                result = validator.validate(this, i);
                Assert.assertEquals("Checked triples: Got an unexpected result for graph " + i, expectedResults[i].checkedTriples,
                        result.checkedTriples);
                Assert.assertEquals("True positives: Got an unexpected result for graph " + i, expectedResults[i].truePositives,
                        result.truePositives);
            }
        }
    }

    @Override
    public int getNumberOfGraphs() {
        return 0;
    }

    @Override
    public Stream<ElementTriplesBlock> getTripleBlocks(int graphId) {
        if (graphId < graphs.length) {
            return GraphBasedTripleBlockStreamCreator.createStreamForGraph(graphs[graphId],
                    new SimpleQueryPatternCreator(graphId, domains, domains));
        } else {
            return null;
        }
    }
}
