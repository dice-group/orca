package org.dice_research.ldcbench.benchmark.eval;

import org.dice_research.ldcbench.graph.Graph;

/**
 * A simple interface that allowes the evaluation to get all necessary
 * information about the generated graphs.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface GraphSupplier {

    /**
     * Getter for the number of graphs.
     * 
     * @return the number of graphs
     */
    public int getNumberOfGraphs();

    /**
     * Getter for the graph with the given ID.
     * 
     * @param id
     *            the ID of the graph
     * @return the graph with the given ID
     */
    public Graph getGraph(int id);

    /**
     * The domains of the graphs. Note that the interface is promises that the
     * domains have the same order as the graphs, i.e., the graph with the ID
     * {@code i} has the {@code i}-th domain in the array.
     * 
     * @return the domain names of the graphs
     */
    public String[] getDomains();
}
