package org.dice_research.ldcbench.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class GrphBasedGraphTest {
    private GraphBuilder g;

    @Before
    public void setUp() {
        g = new GrphBasedGraph();
    }

    @Test
    public void testEmptyGraph() {
        assertEquals("Number of nodes", 0, g.getNumberOfNodes());
        assertEquals("Number of edges", 0, g.getNumberOfEdges());
    }

    @Test
    public void testNumberOfNodes() {
        g.addNode();
        g.addNode();
        assertEquals("Number of nodes", 2, g.getNumberOfNodes());
    }

    @Test
    public void testNumberOfEdges() {
        int n1 = g.addNode();
        int n2 = g.addNode();
        int n3 = g.addNode();
        assertTrue("First edge added successfully", g.addEdge(n1, n2, 0));
        assertTrue("Second edge added successfully", g.addEdge(n1, n3, 0));
        assertEquals("Number of edges", 2, g.getNumberOfEdges());
    }

    @Test
    public void testEdgeWithMissingNode() {
        int n1 = g.addNode();
        assertFalse("Edge wasn't added", g.addEdge(n1, n1 + 1, 0));
        assertFalse("Edge wasn't added", g.addEdge(n1 + 1, n1, 0));
        assertFalse("Edge wasn't added", g.addEdge(n1 + 1, n1 + 2, 0));
        assertEquals("Number of nodes", 1, g.getNumberOfNodes());
        assertEquals("Number of edges", 0, g.getNumberOfEdges());
    }

    @Test
    public void testParallelEdges() {
        int n1 = g.addNode();
        int n2 = g.addNode();
        assertTrue("First edge added successfully", g.addEdge(n1, n2, 1));
        assertTrue("Second edge added successfully", g.addEdge(n1, n2, 2));
        assertEquals("Number of edges", 2, g.getNumberOfEdges());
    }

    @Test
    public void testIdenticalEdges() {
        int n1 = g.addNode();
        int n2 = g.addNode();
        assertTrue("First edge added successfully", g.addEdge(n1, n2, 1));
        assertFalse("Second edge wasn't added", g.addEdge(n1, n2, 1));
        assertEquals("Number of edges", 1, g.getNumberOfEdges());
    }

    @Test
    public void testOutgoingEdgesOrder() {
        int n1 = g.addNode();
        int n2 = g.addNode();
        int n3 = g.addNode();
        assertTrue("First edge added successfully", g.addEdge(n1, n2, 1));
        assertTrue("Second edge added successfully", g.addEdge(n1, n3, 2));
        assertTrue("Unrelated edge added successfully", g.addEdge(n2, n3, 3));
        int[] targets = g.outgoingEdgeTargets(n1);
        int[] types = g.outgoingEdgeTypes(n1);

        assertEquals("Number of targets", 2, targets.length);
        assertEquals("Number of types", 2, types.length);
        if (targets[0] == n2) {
            assertEquals("Second target", n3, targets[1]);
            assertEquals("First type", 1, types[0]);
            assertEquals("Second type", 2, types[1]);
        } else {
            assertEquals("First target", n3, targets[0]);
            assertEquals("Second target", n2, targets[1]);
            assertEquals("First type", 2, types[0]);
            assertEquals("Second type", 1, types[1]);
        }
    }

    @Test
    public void testIncomingEdgesOrder() {
        int n1 = g.addNode();
        int n2 = g.addNode();
        int n3 = g.addNode();
        assertTrue("First edge added successfully", g.addEdge(n1, n3, 1));
        assertTrue("Second edge added successfully", g.addEdge(n2, n3, 2));
        assertTrue("Unrelated edge added successfully", g.addEdge(n1, n2, 3));
        int[] sources = g.incomingEdgeSources(n3);
        int[] types = g.incomingEdgeTypes(n3);

        assertEquals("Number of sources", 2, sources.length);
        assertEquals("Number of types", 2, types.length);
        if (sources[0] == n1) {
            assertEquals("Second source", n2, sources[1]);
            assertEquals("First type", 1, types[0]);
            assertEquals("Second type", 2, types[1]);
        } else {
            assertEquals("First source", n2, sources[0]);
            assertEquals("Second source", n1, sources[1]);
            assertEquals("First type", 2, types[0]);
            assertEquals("Second type", 1, types[1]);
        }
    }

    @Test
    public void testDuplicateNodes() {
        assertTrue("Node added successfully", ((GrphBasedGraph) g).addNode(0));
        assertFalse("Duplicate node wasn't added", ((GrphBasedGraph) g).addNode(0));
        assertEquals("Number of nodes", 1, g.getNumberOfNodes());
    }
}
