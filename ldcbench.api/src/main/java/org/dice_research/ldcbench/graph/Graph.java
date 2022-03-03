package org.dice_research.ldcbench.graph;

import org.dice_research.ldcbench.rdf.RDFNodeType;

/**
 * A simple graph interface for the graphs LDC-bench is using. The nodes are
 * represented by IDs. Edges are directed and represented using the nodes IDs of
 * source and target as well as their type ID.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface Graph {

    /**
     * The graph ID of internal nodes.
     */
    public static final int INTERNAL_NODE_GRAPH_ID = -1;

    /**
     * Returns an array containing the target node IDs of the edges that have the
     * given node as source. Note that this method returns the outgoing edges in the
     * order as the {@link #outgoingEdgeTypes(int)} method.
     * 
     * @param nodeId the source node ID of the edges for which the target IDs should
     *               be returned
     * @return the target IDs of the edges
     */
    public int[] outgoingEdgeTargets(int nodeId);

    /**
     * Returns an array containing the types of the outgoing edges of the given
     * node. Note that this method returns the outgoing edges in the order as the
     * {@link #outgoingEdgeTargets(int)} method.
     * 
     * @param nodeId the source node ID of the edges for which the type IDs should
     *               be returned
     * @return the type IDs of the edges
     */
    public int[] outgoingEdgeTypes(int nodeId);

    /**
     * Returns the number of outgoing edges of the given node.
     * 
     * @param nodeId the source node ID of the edges that should be counted
     * @return the number of outgoing edges
     */
    public default int outgoingEdgeCount(int nodeId) {
        int outgoingEdgeTypes[] = outgoingEdgeTypes(nodeId);
        return outgoingEdgeTypes == null ? 0 : outgoingEdgeTypes.length;
    }

    /**
     * Returns an array containing the source node IDs of the edges that have the
     * given node as target. Note that this method returns the incoming edges in the
     * order as the {@link #incomingEdgeTypes(int)} method.
     * 
     * @param nodeId the target node ID of the edges for which the source IDs should
     *               be returned
     * @return the source IDs of the edges
     */
    public int[] incomingEdgeSources(int nodeId);

    /**
     * Returns an array containing the types of the incoming edges of the given
     * node. Note that this method returns the incoming edges in the order as the
     * {@link #incomingEdgeSources(int)} method.
     * 
     * @param nodeId the target node ID of the edges for which the type IDs should
     *               be returned
     * @return the type IDs of the edges
     */
    public int[] incomingEdgeTypes(int nodeId);

    /**
     * Returns the number of incoming edges of the given node.
     * 
     * @param nodeId the target node ID of the edges that should be counted
     * @return the number of incoming edges
     */
    public default int incomingEdgeCount(int nodeId) {
        int incomingEdgeTypes[] = incomingEdgeTypes(nodeId);
        return incomingEdgeTypes == null ? 0 : incomingEdgeTypes.length;
    }

    /**
     * Returns the number of nodes of this graph.
     * 
     * @return the number of nodes of this graph.
     */
    public int getNumberOfNodes();

    /**
     * Returns the number of edges of this graph.
     * 
     * @return the number of edges of this graph.
     */
    public int getNumberOfEdges();

    /**
     * Returns the IDs of the entrance nodes (i.e., nodes from which all other nodes
     * can be reached).
     * 
     * @return the IDs of the entrance nodes
     */
    public int[] getEntranceNodes();

    /**
     * Returns the ID of the graph the given node is part of. If the given node is
     * an internal node (i.e., the node is part of this graph), the ID equals
     * {@link #INTERNAL_NODE_GRAPH_ID}={@value #INTERNAL_NODE_GRAPH_ID}. If the
     * given node is an external node, i.e., the node is not part of this graph but
     * there is an edge from one of the internal nodes to this node, the method
     * returns the ID of the external graph.
     * 
     * @param nodeId the ID of the node that should be checked.
     * @return the ID for the internal graph
     *         ({@link #INTERNAL_NODE_GRAPH_ID}={@value #INTERNAL_NODE_GRAPH_ID}) or
     *         the ID of the external graph the node belongs to.
     */
    public int getGraphId(int nodeId);
    
    public int getGraphId();

    /**
     * Returns the ID of the node in the external graph if this node is an external
     * node. If the node is internal, its internal ID (the given id) is returned.
     * 
     * @param nodeId the ID of the node that should be checked.
     * @return the external ID of the node in the other graph.
     */
    public int getExternalNodeId(int nodeId);

    /**
     * Returns the range of Blank Nodes of the graph
     *
     * @return the range of Blank Nodes of the graph
     */
    public int[] getBlankNodesRange();

    /**
     * Returns the range of literals of the graph
     *
     * @return the range of literals of the graph
     */
    public int[] getLiteralsRange();

    /**
     * Check if a node is a blank node
     *
     * @param nodeId the ID of the node to check
     * @return true if node with given nodeId is blank node and false otherwise
     */
    public boolean isBlankNode(int nodeId);

    /**
     * Check if a node is a literal
     *
     * @param nodeId the ID of the node to check
     * @return true if node with given nodeId is literal and false otherwise
     */
    public boolean isLiteral(int nodeId);

    /**
     * Returns the number of IRI nodes of the graph
     *
     * @return the number of IRI nodes of the graph
     */
    public int getNumberOfIriNodes();

    /**
     * Return the type of a given node
     * @param nodeId the id of the node
     * @return the type of the node with the given id
     */
    public RDFNodeType getNodeType(int nodeId);
}