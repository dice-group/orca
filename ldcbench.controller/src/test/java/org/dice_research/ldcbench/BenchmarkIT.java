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
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }

    @Override
    public Model createBenchmarkParameters() throws IOException {
        Model model = super.createBenchmarkParameters();
        Resource experimentResource = model.getResource(org.hobbit.core.Constants.NEW_EXPERIMENT_URI);
        model.add(experimentResource, LDCBench.numberOfNodes, "3", XSDinteger);
        model.add(experimentResource, LDCBench.averageNodeGraphDegree, "3", XSDinteger);
        model.add(experimentResource, LDCBench.dereferencingHttpNodeWeight, "1", XSDfloat);
        model.add(experimentResource, LDCBench.ckanNodeWeight, "0.01", XSDfloat);
        return model;
    }
}
