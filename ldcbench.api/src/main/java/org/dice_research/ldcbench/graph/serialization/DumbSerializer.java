package org.dice_research.ldcbench.graph.serialization;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

/**
 * A dumb implementation of Serializer interface.
 */
public class DumbSerializer implements Serializer {

    public byte[] serialize(Graph graph) {
        // count number of external nodes
        int numberOfNodes = graph.getNumberOfNodes();
        int numberOfExternalNodes = (int) IntStream.range(0, numberOfNodes)
                .filter(i -> graph.getGraphId(i) != Graph.INTERNAL_NODE_GRAPH_ID).count();
        int[] entranceNodes = graph.getEntranceNodes();

        // + 4 for the number of nodes, edges, external nodes and entrance nodes
        // edges * 3 since we have to store the source, type and target
        // external nodes * 3 since we have to store the internal id, external id and
        // graph id
        ByteBuffer buf = ByteBuffer.allocate(
                (4 + graph.getNumberOfEdges() * 3 + numberOfExternalNodes * 3 + entranceNodes.length) * Integer.BYTES);
        buf.putInt(numberOfNodes);
        buf.putInt(graph.getNumberOfEdges());
        buf.putInt(numberOfExternalNodes);
        buf.putInt(entranceNodes.length);

        for (int i = 0; i < numberOfNodes; i++) {
            int[] targets = graph.outgoingEdgeTargets(i);
            int[] types = graph.outgoingEdgeTypes(i);
            for (int j = 0; j < targets.length; j++) {
                buf.putInt(i);
                buf.putInt(types[j]);
                buf.putInt(targets[j]);
            }
        }
        if (numberOfExternalNodes > 0) {
            for (int i = 0; i < numberOfNodes; i++) {
                if (graph.getGraphId(i) != Graph.INTERNAL_NODE_GRAPH_ID) {
                    buf.putInt(i);
                    buf.putInt(graph.getGraphId(i));
                    buf.putInt(graph.getExternalNodeId(i));
                }
            }
        }
        for (int i = 0; i < entranceNodes.length; i++) {
            buf.putInt(entranceNodes[i]);
        }
        return buf.array();
    }

    public Graph deserialize(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        int nodes = buf.getInt();
        int edges = buf.getInt();
        int numberOfExternalNodes = buf.getInt();
        int numberOfEntranceNodes = buf.getInt();
        GrphBasedGraph graph = new GrphBasedGraph();

        for (int n = 0; n < nodes; n++) {
            graph.addNode();
        }

        int src, type, dest;
        for (int j = 0; j < edges; j++) {
            src = buf.getInt();
            type = buf.getInt();
            dest = buf.getInt();
            graph.addEdge(src, dest, type);
        }
        int nodeId, graphId, externalId;
        for (int i = 0; i < numberOfExternalNodes; ++i) {
            nodeId = buf.getInt();
            graphId = buf.getInt();
            externalId = buf.getInt();
            graph.setGraphIdOfNode(nodeId, graphId, externalId);
        }
        int[] entranceNodes = new int[numberOfEntranceNodes];
        for (int i = 0; i < numberOfEntranceNodes; ++i) {
            entranceNodes[i] = buf.getInt();
        }
        graph.setEntranceNodes(entranceNodes);

        return graph;
    }

}
