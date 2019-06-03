package org.dice_research.ldcbench.rdf;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SimpleTripleCreatorTest extends AbstractTripleCreatorTest {

    @Parameters
    public static List<Object[]> testCases() {
        List<Object[]> data = new ArrayList<>();

        // 1 graph, one edge from a resource to itself
        data.add(new Object[] { 0, new String[] { "domain0.org" }, new int[] { 0, 0, 0, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-0",
                        "http://domain0.org/dataset-0/resource-0" } });

        // 1 graph, one edge from a resource to another resource
        data.add(new Object[] { 0, new String[] { "domain0.org" }, new int[] { 0, 0, 1, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-0",
                        "http://domain0.org/dataset-0/resource-1" } });
        data.add(new Object[] { 0, new String[] { "domain0.org" }, new int[] { 0, 1, 1, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-1",
                        "http://domain0.org/dataset-0/resource-1" } });
        data.add(new Object[] { 0, new String[] { "domain0.org" }, new int[] { 1, 0, 0, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-1", "http://domain0.org/dataset-0/property-0",
                        "http://domain0.org/dataset-0/resource-0" } });

        // 2 graphs, one edge in one graph from a resource to another resource in the same graph
        data.add(new Object[] { 0, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 0, 1, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-0",
                        "http://domain0.org/dataset-0/resource-1" } });
        data.add(new Object[] { 1, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 0, 1, -1, -1 },
                new String[] { "http://domain1.org/dataset-0/resource-0", "http://domain1.org/dataset-0/property-0",
                        "http://domain1.org/dataset-0/resource-1" } });

        // 2 graphs, one edge in one graph from a resource to another resource in the same graph
        data.add(new Object[] { 0, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 0, 1, -1, -1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-0",
                        "http://domain0.org/dataset-0/resource-1" } });
        data.add(new Object[] { 1, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 0, 1, -1, -1 },
                new String[] { "http://domain1.org/dataset-0/resource-0", "http://domain1.org/dataset-0/property-0",
                        "http://domain1.org/dataset-0/resource-1" } });

        // 2 graphs, one edge in one graph from a resource to another resource in the other graph
        data.add(new Object[] { 0, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 0, 1, 19, 1 },
                new String[] { "http://domain0.org/dataset-0/resource-0", "http://domain0.org/dataset-0/property-0",
                        "http://domain1.org/dataset-0/resource-19" } });
        data.add(new Object[] { 1, new String[] { "domain0.org", "domain1.org" }, new int[] { 0, 1, 1, 21, 0 },
                new String[] { "http://domain1.org/dataset-0/resource-0", "http://domain1.org/dataset-0/property-1",
                        "http://domain0.org/dataset-0/resource-21" } });

        return data;
    }

    public SimpleTripleCreatorTest(int baseGraphId, String[] resourceUriTemplates, String[] accessUriTemplates, int edge[], String expectedUris[]) {
        super(new SimpleTripleCreator(baseGraphId, resourceUriTemplates, accessUriTemplates), edge, expectedUris);
    }

}
