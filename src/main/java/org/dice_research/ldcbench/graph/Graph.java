package org.dice_research.ldcbench.graph;

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
     * Returns an array containing the target node IDs of the edges that have the
     * given node as source. Note that this method returns the outgoing edges in the
     * order as the {@link #outgoingEdgeTypes(int)} method.
     * 
     * @param nodeId
     *            the source node ID of the edges for which the target IDs should be
     *            returned
     * @return the target IDs of the edges
     */
    public int[] outgoingEdgeTargets(int nodeId);

    /**
     * Returns an array containing the types of the outgoing edges of the given
     * node. Note that this method returns the outgoing edges in the order as the
     * {@link #outgoingEdgeTargets(int)} method.
     * 
     * @param nodeId
     *            the source node ID of the edges for which the type IDs should be
     *            returned
     * @return the type IDs of the edges
     */
    public int[] outgoingEdgeTypes(int nodeId);
    
    /**
     * Returns an array containing the source node IDs of the edges that have the
     * given node as target. Note that this method returns the incoming edges in the
     * order as the {@link #incomingEdgeTypes(int)} method.
     * 
     * @param nodeId
     *            the target node ID of the edges for which the source IDs should be
     *            returned
     * @return the source IDs of the edges
     */
    public int[] incomingEdgeSources(int nodeId);

    /**
     * Returns an array containing the types of the incoming edges of the given
     * node. Note that this method returns the incoming edges in the order as the
     * {@link #incomingEdgeSources(int)} method.
     * 
     * @param nodeId
     *            the target node ID of the edges for which the type IDs should be
     *            returned
     * @return the type IDs of the edges
     */
    public int[] incomingEdgeTypes(int nodeId);
}
