package org.dice_research.ldcbench;

import java.io.IOException;
import static org.apache.jena.datatypes.xsd.XSDDatatype.*;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

import org.dice_research.ldcbench.vocab.LDCBench;

/**
 * @author Pavel Smirnov
 */

public class BenchmarkIT extends BenchmarkTestBase {
    @Test
    public void executeBenchmark() throws Exception {
        executeBenchmark(true);
    }

    @Override
    public Model createBenchmarkParameters() throws IOException {
        Model model = super.createBenchmarkParameters();
        Resource experimentResource = org.hobbit.vocab.HobbitExperiments.New;
        model.add(experimentResource, LDCBench.numberOfNodes, "4", XSDinteger);
        model.add(experimentResource, LDCBench.averageNodeGraphDegree, "3", XSDinteger);
        return model;
    }
}
