package org.dice_research.ldcbench.graph;

/**
 * An extension of the {@link Graph} interface offering methods for the addition
 * of nodes and edges.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface GraphBuilder extends Graph {

    /**
     * Returns a {@link Graph} instance based on the graph data that the builder
     * collected until now. This instance might offer a faster implementation than
     * the builder class.
     * 
     * @return a {@link Graph} instance containing the built graph.
     */
    public default Graph build() {
        return this;
    }

    /**
     * Adds a new edge with the given information if possible. Returns {@code true}
     * if the edge could be added.
     * 
     * @param sourceId
     *            the id of the source node
     * @param targetId
     *            the id of the target node
     * @param typeId
     *            the type id of the edge
     * @return {@code true} if the edge could be added. Otherwise {@code false} is
     *         returned.
     */
    public boolean addEdge(int sourceId, int targetId, int typeId);

    /**
     * Adds a new node to the graph and returns its ID.
     * 
     * @return the ID of the newly created node
     */
    public int addNode();

    /**
     * Adds the given number of nodes to the graph and returns the range of node
     * IDs.
     * 
     * @param nodeCount
     *            the number of nodes that should be added
     * @return the range of IDs from the first added node ([0], inclusive) to the first
     *         node after the last added node ([1], exclusive).
     */
    public int[] addNodes(int nodeCount);
}
