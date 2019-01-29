package org.dice_research.ldcbench.graph;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.ArrayList;
import grph.in_memory.InMemoryGrph;
import grph.Grph;

public class GrphBasedGraph implements GraphBuilder {

    protected Grph graph = new InMemoryGrph();
    protected ArrayList<Integer> edgeTypes = new ArrayList<>();

    /**
     * Given a vertice ID and edge ID, returns the other vertice ID
     * of a vertice which is also incident to that edge.
     *
     * @param thisVertice
     *            the vertice ID
     * @param edge
     *            the edge ID
     * @return the vertice ID which is not equal to thisVertice
     */
    private IntUnaryOperator getOtherVerticeIncidentToEdge(int thisVertice) {
        return (edge) -> {
            for (int vertice : graph.getVerticesIncidentToEdge(edge)) {
                if (vertice != thisVertice) {
                    return vertice;
                }
            }
            throw new IllegalStateException("No other vertice found for edge");
        };
    }

    /**
     * Given an edge ID, returns type of that edge.
     *
     * @param edge
     *            the edge ID
     * @return the edge type
     */
    private int getEdgeType(int edge) {
        return edgeTypes.get(edge);
    }

    /**
     * Returns a Stream containing edges that have the given node as source
     * sorted by edge ID. Used to construct arrays of edge targets
     * and edge types in the same order.
     *
     * @param nodeId
     *            the source node ID of the edges
     * @return the IDs of the edges
     */
    private IntStream orderedOutgoingEdges(int nodeId) {
        int[] edges = graph.getOutEdges(nodeId).toIntArray();
        Arrays.sort(edges);
        return Arrays.stream(edges);
    }

    /**
     * Returns a Stream containing edges that have the given node as target
     * sorted by edge ID. Used to construct arrays of edge sources
     * and edge types in the same order.
     *
     * @param nodeId
     *            the target node ID of the edges
     * @return the IDs of the edges
     */
    private IntStream orderedIncomingEdges(int nodeId) {
        int[] edges = graph.getInEdges(nodeId).toIntArray();
        Arrays.sort(edges);
        return Arrays.stream(edges);
    }

    public int[] outgoingEdgeTargets(int nodeId) {
        return orderedOutgoingEdges(nodeId).map(getOtherVerticeIncidentToEdge(nodeId)).toArray();
    }

    public int[] outgoingEdgeTypes(int nodeId) {
        return orderedOutgoingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    public int[] incomingEdgeSources(int nodeId) {
        return orderedIncomingEdges(nodeId).map(getOtherVerticeIncidentToEdge(nodeId)).toArray();
    }

    public int[] incomingEdgeTypes(int nodeId) {
        return orderedIncomingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    public int getNumberOfNodes() {
        return graph.getNumberOfVertices();
    }

    public int getNumberOfEdges() {
        return graph.getNumberOfEdges();
    }

    public boolean addEdge(int sourceId, int targetId, int typeId) {
        // Do not add the edge if any of incident vertices are missing.
        if (!(graph.containsVertex(sourceId) && graph.containsVertex(targetId))) {
            return false;
        }

        // Do not add the edge if there's already another edge with the same type.
        for (int edge : graph.getEdgesConnecting(sourceId, targetId)) {
            if (edgeTypes.get(edge) == typeId) {
                return false;
            }
        }

        int edge = graph.addSimpleEdge(sourceId, targetId, true);
        try {
            edgeTypes.set(edge, typeId);
        } catch (IndexOutOfBoundsException e) {
            edgeTypes.add(edge, typeId);
        }
        return true;
    }

    public int addNode() {
        return graph.addVertex();
    }

}
