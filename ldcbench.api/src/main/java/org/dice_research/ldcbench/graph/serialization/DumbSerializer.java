package org.dice_research.ldcbench.graph.serialization;

import java.nio.ByteBuffer;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

/**
 * A dumb implementation of Serializer interface.
 */
public class DumbSerializer implements Serializer {

    public byte[] serialize(Graph graph) {
        ByteBuffer buf = ByteBuffer.allocate((2 + graph.getNumberOfEdges() * 3) * Integer.SIZE);
        buf.putInt(graph.getNumberOfNodes());
        buf.putInt(graph.getNumberOfEdges());

        int nodes = graph.getNumberOfNodes();
        for (int i = 0; i < nodes; i++) {
            int[] targets = graph.outgoingEdgeTargets(i);
            int[] types = graph.outgoingEdgeTypes(i);
            for (int j = 0; j < targets.length; j++) {
                buf.putInt(i);
                buf.putInt(types[j]);
                buf.putInt(targets[j]);
            }
        }
        return buf.array();
    }

    public Graph deserialize(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        /*int nodes =*/ buf.getInt();
        int edges = buf.getInt();
        GrphBasedGraph graph = new GrphBasedGraph();

        for (int j = 0; j < edges; j++) {
            int src = buf.getInt();
            int type = buf.getInt();
            int dest = buf.getInt();
            graph.addNode(src);
            graph.addNode(dest);
            graph.addEdge(src, dest, type);
        }
        return graph;
    }

}
