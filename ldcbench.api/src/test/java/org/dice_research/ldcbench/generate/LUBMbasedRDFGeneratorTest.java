package org.dice_research.ldcbench.generate;

import static org.junit.Assert.assertEquals;

import org.apache.jena.ext.com.google.common.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class LUBMbasedRDFGeneratorTest {

    LUBMbasedRDFGenerator lubmGen;
    GrphBasedGraph g;
    private final int VERTICES_PER_DEPT = 1300;
    private final int EDGES_PER_DEPT = 4700;

    @Before
    public void setUp() {
        lubmGen = new LUBMbasedRDFGenerator();
        g = new GrphBasedGraph();
    }

    @Test
    public void graphWithOneDeptOfOneUni() {
        lubmGen.generateGraph(1000, 0.5, 15L, g);
        Assert.assertTrue("Generated graph is empty", 0 < g.getNumberOfNodes());
        Assert.assertTrue("Generated graph's size is less than required", 1000 < g.getNumberOfNodes());
        Assert.assertTrue("Generated graph's size exceed expected size", g.getNumberOfNodes() < 2* VERTICES_PER_DEPT);

        g = new GrphBasedGraph();
        lubmGen.generateGraph(0.5, 2000, 15L, g);
        Assert.assertTrue("Generated graph is empty", 0 < g.getNumberOfEdges());
        Assert.assertTrue("Generated graph's size is less than required", 2000 < g.getNumberOfEdges());
        Assert.assertTrue("Generated graph's size exceed expected size", g.getNumberOfNodes() < 2* EDGES_PER_DEPT);
    }
}
