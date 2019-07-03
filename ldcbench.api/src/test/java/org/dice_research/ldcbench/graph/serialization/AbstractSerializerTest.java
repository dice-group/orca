package org.dice_research.ldcbench.graph.serialization;

import java.util.Arrays;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSerializerTest {

    private Class<? extends Serializer> serializerClass;

    public AbstractSerializerTest(Class<? extends Serializer> serializerClass) {
        this.serializerClass = serializerClass;
    }

    @Test
    public void testSimpleGraph() throws Exception {
        GraphBuilder g1 = new GrphBasedGraph();
        int n1 = g1.addNode();
        int n2 = g1.addNode();
        int n3 = g1.addNode();
        g1.addEdge(n1, n2, 1);
        g1.addEdge(n1, n2, 2);
        g1.addEdge(n1, n3, 1);
        Graph g2 = SerializationHelper.deserialize(SerializationHelper.serialize(serializerClass, g1));
        checkGraphs(g1, g2);
    }

    @Test
    public void testComplexGraph() throws Exception {
        GraphBuilder g1 = new GrphBasedGraph();
        int n1 = g1.addNode();
        int n2 = g1.addNode();
        int n3 = g1.addNode();
        g1.addEdge(n1, n2, 0);
        g1.addEdge(n1, n2, 2);
        g1.addEdge(n1, n3, 1);
        g1.addEdge(n2, n3, 0);
        // set entrance nodes
        g1.setEntranceNodes(new int[] { n1, n2 });
        // define n3 as external node
        g1.setGraphIdOfNode(n3, 9, 14);
        
        Graph g2 = SerializationHelper.deserialize(SerializationHelper.serialize(serializerClass, g1));
        checkGraphs(g1, g2);
    }

    @Test
    public void testNodeWithNoEdges() throws Exception {
         GraphBuilder g1 = new GrphBasedGraph();
         g1.addNode();
         byte[] data = SerializationHelper.serialize(DumbSerializer.class, g1);
         Graph g2 = SerializationHelper.deserialize(data);
         checkGraphs(g1, g2);
    }

    public void checkGraphs(Graph g1, Graph g2) {
        // Check graph sizes
        Assert.assertEquals("Number of nodes is not equal", g1.getNumberOfNodes(), g2.getNumberOfNodes());
        Assert.assertEquals("Number of edges is not equal", g1.getNumberOfEdges(), g2.getNumberOfEdges());

        for (int i = 0; i < g1.getNumberOfNodes(); ++i) {
            // Check external node information
            Assert.assertEquals("Wrong external Id for node " + i, g1.getExternalNodeId(i), g2.getExternalNodeId(i));
            Assert.assertEquals("Wrong graph Id for node " + i, g1.getGraphId(i), g2.getGraphId(i));
            // Check edges
            assertEdgesEqual("Wrong outgoing edges for node " + i,
                    g1.outgoingEdgeTargets(i), g1.outgoingEdgeTypes(i),
                    g2.outgoingEdgeTargets(i), g2.outgoingEdgeTypes(i));
            assertEdgesEqual("Wrong incoming edges for node " + i,
                    g1.incomingEdgeSources(i), g1.incomingEdgeTypes(i),
                    g2.incomingEdgeSources(i), g2.incomingEdgeTypes(i));
        }
        Assert.assertArrayEquals("Entrance nodes are not equal", g1.getEntranceNodes(), g2.getEntranceNodes());
    }

    private void assertEdgesEqual(String message, int[] vertices1, int[] types1, int[] vertices2, int[] types2) {
        Assert.assertEquals("Amount of vertices and types for the first graph", vertices1.length, types1.length);
        Assert.assertEquals("Amount of vertices and types for the first graph", vertices2.length, types2.length);
        Assert.assertEquals("Amount of vertices in both graphs", vertices1.length, vertices2.length);

        int n = vertices1.length;

        String[] s1 = new String[n];
        String[] s2 = new String[n];
        for (int i = 0; i < n; i++) {
            s1[i] = vertices1[i] + ":" + types1[i];
            s2[i] = vertices2[i] + ":" + types2[i];
        }
        Arrays.sort(s1);
        Arrays.sort(s2);
        Assert.assertArrayEquals(message, s1, s2);
    }
}
