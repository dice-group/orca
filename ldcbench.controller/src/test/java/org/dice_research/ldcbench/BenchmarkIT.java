package org.dice_research.ldcbench;

import java.io.IOException;
import static org.apache.jena.datatypes.xsd.XSDDatatype.*;
import org.apache.jena.rdf.model.*;
import org.hobbit.sdk.utils.ModelsHandler;
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
        return ModelsHandler.readModelFromFile("integration-benchmark-parameters.ttl");
    }
}
