package org.dice_research.ldcbench.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.dice_research.ldcbench.rdf.RDFNodeType;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GrphBasedGraph implements GraphBuilder {

    /**
     * Index of the graph ID in the array used as value in {@link #externalNodes}.
     */
    protected static final int EXTERNAL_NODE_GRAPH_ID_INDEX = 0;
    /**
     * Index of the external node ID in the array used as value in
     * {@link #externalNodes}.
     */
    protected static final int EXTERNAL_NODE_NODE_ID_INDEX = 1;

    protected Grph graph = new InMemoryGrph();
    protected ArrayList<Integer> edgeTypes = new ArrayList<>();
    /**
     * Entrance nodes of this graph.
     */
    protected int[] entranceNodes = new int[0];
    /**
     * Storage of additional information (graph ID and node ID in this graph) of
     * external nodes.
     */
    protected Map<Integer, int[]> externalNodes = new HashMap<>();
    /**
     * Index of first blank node of this graph.
     */
    protected int blankNodesIndex = Integer.MAX_VALUE;

    protected int actualGraphId;

    protected int[] blankNodesRange = {Integer.MAX_VALUE,Integer.MAX_VALUE};
    /**
     * Index of first literal of this graph.
     */
    protected int[] literalsRange = {Integer.MAX_VALUE,Integer.MAX_VALUE};
    /**
     * Constructor for an empty GraphBuilder.
     */
    public GrphBasedGraph() {
    }

    /**
     * Clones speficied graph's data into this GraphBuilder.
     */
    public GrphBasedGraph(Graph other) {
        int nodes = other.getNumberOfNodes();
        addNodes(nodes);
        for (int i = 0; i < nodes; i++) {
            int[] targets = other.outgoingEdgeTargets(i);
            int[] types = other.outgoingEdgeTypes(i);
            for (int j = 0; j < targets.length; j++) {
                addEdge(i, targets[j], types[j]);
            }
            if (other.getGraphId(i) != Graph.INTERNAL_NODE_GRAPH_ID) {
                setGraphIdOfNode(i, other.getGraphId(i), other.getExternalNodeId(i));
            }
        }
        setEntranceNodes(other.getEntranceNodes());
        this.blankNodesRange = other.getBlankNodesRange().clone();
        this.literalsRange = other.getLiteralsRange().clone();
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
     * Returns a Stream containing edges that have the given node as source sorted
     * by edge ID. Used to construct arrays of edge targets and edge types in the
     * same order.
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
     * Returns a Stream containing edges that have the given node as target sorted
     * by edge ID. Used to construct arrays of edge sources and edge types in the
     * same order.
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
        return orderedOutgoingEdges(nodeId).map(graph::getDirectedSimpleEdgeHead).toArray();
    }

    public int[] outgoingEdgeTypes(int nodeId) {
        return orderedOutgoingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    @Override
    public int outgoingEdgeCount(int nodeId) {
        return graph.getOutEdgeDegree(nodeId);
    }

    public int[] incomingEdgeSources(int nodeId) {
        return orderedIncomingEdges(nodeId).map(graph::getDirectedSimpleEdgeTail).toArray();
    }

    public int[] incomingEdgeTypes(int nodeId) {
        return orderedIncomingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    @Override
    public int incomingEdgeCount(int nodeId) {
        return graph.getInEdgeDegree(nodeId);
    }

    public int getNumberOfNodes() {
        return graph.getNumberOfVertices();
    }

    public int getNumberOfEdges() {
        return graph.getNumberOfEdges();
    }

    public int getNumberOfIriNodes() {
        int numberOfBlankNode = blankNodesRange[1] - blankNodesRange[0];
        int numberOfLiterals = literalsRange[1] - literalsRange[0];
        return this.getNumberOfNodes() - numberOfBlankNode - numberOfLiterals;
    }

    @Override
    public int[] getBlankNodesRange() {
        return blankNodesRange;
    }

    @Override
    public int[] getLiteralsRange() {
        return literalsRange;
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
            // Assume new edge IDs are sequential. Otherwise this will throw.
            edgeTypes.add(edge, typeId);
        }
        return true;
    }

    public int addNode() {
        return graph.addVertex();
    }

    /**
     * Adds a new node with specified ID to the graph. Returns {@code true} if the
     * node could be added.
     *
     * @param nodeId
     *            the id of the node
     * @return {@code true} if the node could be added. Otherwise {@code false} is
     *         returned.
     */
    public boolean addNode(int nodeId) {
        if (graph.containsVertex(nodeId)) {
            return false;
        }

        graph.addVertex(nodeId);
        return true;
    }

    @Override
    public int[] addNodes(int nodeCount) {
        IntSet ids = graph.addNVertices(nodeCount);
        int min = Integer.MAX_VALUE, max = -1;
        IntIterator iterator = ids.iterator();
        int id;
        while (iterator.hasNext()) {
            id = iterator.nextInt();
            if (min > id) {
                min = id;
            }
            if (max < id) {
                max = id;
            }
        }
        return new int[] { min, max + 1 };
    }

    @Override
    public void setEntranceNodes(int[] entranceNodes) {
        this.entranceNodes = entranceNodes;
    }

    @Override
    public int[] getEntranceNodes() {
        return entranceNodes;
    }

    @Override
    public int getGraphId(int nodeId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_GRAPH_ID_INDEX)) {
                return externalNode[EXTERNAL_NODE_GRAPH_ID_INDEX];
            }
        }
        return INTERNAL_NODE_GRAPH_ID;
    }

    @Override
    public int getExternalNodeId(int nodeId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_NODE_ID_INDEX)) {
                return externalNode[EXTERNAL_NODE_NODE_ID_INDEX];
            }
        }
        return nodeId;
    }

    @Override
    public void setGraphIdOfNode(int nodeId, int graphId, int externalId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            // Reuse the existing array if possible
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_NODE_ID_INDEX)) {
                externalNode[EXTERNAL_NODE_GRAPH_ID_INDEX] = graphId;
                externalNode[EXTERNAL_NODE_NODE_ID_INDEX] = externalId;
                // The information has been updated. So let's leave the method
                return;
            }
        }
        // There is nothing we can reuse so create a new array
        externalNodes.put(n, new int[] { graphId, externalId });
    }

    @Override
    public String toString() {
        return graph.toString();
    }

    public int getGraphId() {
    	return actualGraphId;
    }

    public void setGraphId(int id) {
        actualGraphId = id;
    }

    @Override
    public boolean isBlankNode(int nodeId) {
        return nodeId >= getBlankNodesRange()[0] && nodeId < getBlankNodesRange()[1];
    }

    @Override
    public void setBlankNodesRange(int firstBnodeId, int bNodesCount) {
        blankNodesRange[0] = firstBnodeId;
        blankNodesRange[1] = firstBnodeId + bNodesCount;
    }

    @Override
    public void setLiteralsRange(int firstLiteralId, int literalsCount) {
        literalsRange[0] = firstLiteralId;
        literalsRange[1] = firstLiteralId + literalsCount;
    }

    @Override
    public boolean isLiteral(int nodeId) {
        return nodeId >= getLiteralsRange()[0] && nodeId < getLiteralsRange()[1];
    }

    @Override
    public RDFNodeType getNodeType(int nodeId) {
        if (this.isBlankNode(nodeId))
            return RDFNodeType.BlankNode;
        if (this.isLiteral(nodeId))
            return RDFNodeType.Literal;
        return RDFNodeType.IRI;
    }
}
