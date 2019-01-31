package org.dice_research.ldcbench.graph.serialization;

import org.dice_research.ldcbench.graph.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DumbSerializerTest {
    @Test
    public void test() throws Exception {
        GraphBuilder g1 = new GrphBasedGraph();
        int n1 = g1.addNode();
        int n2 = g1.addNode();
        int n3 = g1.addNode();
        g1.addEdge(n1, n2, 1);
        g1.addEdge(n1, n2, 2);
        g1.addEdge(n1, n3, 1);
        assertEquals("Number of nodes", 3, g1.getNumberOfNodes());
        assertEquals("Number of edges", 3, g1.getNumberOfEdges());

        Graph g2 = SerializationHelper.deserialize(SerializationHelper.serialize(DumbSerializer.class, g1));
        assertEquals("Number of nodes in deserialized graph", 3, g2.getNumberOfNodes());
        assertEquals("Number of edges in deserialized graph", 3, g2.getNumberOfEdges());
    }
}
