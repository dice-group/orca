package org.dice_research.ldcbench.benchmark.eval.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_research.ldcbench.benchmark.eval.sparql.SparqlBasedTripleCounter;
import org.junit.Assert;
import org.junit.Test;

public class SparqlBasedTripleCounterTest {


    @Test
    public void test() throws Exception {
        Dataset dataset = DatasetFactory.create();
        
        try (SparqlBasedTripleCounter counter = new SparqlBasedTripleCounter(
                new QueryExecutionFactoryDataset(dataset), QueryFactory.create(SparqlBasedTripleCounter.COUNT_QUERY))) {
            // The dataset should be empty
            Assert.assertEquals(0, counter.countTriples());
            
            // Add a graph with some triples
            Model model1 = ModelFactory.createDefaultModel();
            model1.add(model1.getResource("http://domain1.org/dataset-0/resource-0"),
                    model1.getProperty("http://domain1.org/dataset-0/property-0"),
                    model1.getResource("http://domain1.org/dataset-0/resource-1"));
            model1.add(model1.getResource("http://domain1.org/dataset-0/resource-0"),
                    model1.getProperty("http://domain1.org/dataset-0/property-1"),
                    model1.getResource("http://domain1.org/dataset-0/resource-1"));
            model1.add(model1.getResource("http://domain1.org/dataset-0/resource-0"),
                    model1.getProperty("http://domain1.org/dataset-0/property-2"),
                    model1.getResource("http://domain0.org/dataset-0/resource-0"));
            dataset.addNamedModel("http://domain1.org", model1);
            
            Assert.assertEquals(3, counter.countTriples());
            
            // Add a second graph with the same triples as the first graph
            Model model2 = ModelFactory.createDefaultModel();
            model2.add(model2.getResource("http://domain1.org/dataset-0/resource-0"),
                    model2.getProperty("http://domain1.org/dataset-0/property-0"),
                    model2.getResource("http://domain1.org/dataset-0/resource-1"));
            model2.add(model2.getResource("http://domain1.org/dataset-0/resource-0"),
                    model2.getProperty("http://domain1.org/dataset-0/property-1"),
                    model2.getResource("http://domain1.org/dataset-0/resource-1"));
            model2.add(model2.getResource("http://domain1.org/dataset-0/resource-0"),
                    model2.getProperty("http://domain1.org/dataset-0/property-2"),
                    model2.getResource("http://domain0.org/dataset-0/resource-0"));
            dataset.addNamedModel("http://domain2.org", model2);
            
            Assert.assertEquals(6, counter.countTriples());
        }
    }
}
