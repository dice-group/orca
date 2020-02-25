package org.dice_research.ldcbench.benchmark.eval.supplier.pattern;

import java.util.stream.Stream;

import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 * The interface of a class that is able to provide a stream of
 * {@link ElementTriplesBlock} instances that can be used to check the
 * completeness of a graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface TripleBlockStreamSupplier {

    /**
     * Returns a stream of triple blocks used to query the data of the graph with
     * the given id.
     * 
     * @param id the id of the graph for which the stream of triple blocks should be
     *           generated
     * @return the stream of triple blocks used to evaluate the graph
     */
    public Stream<ElementTriplesBlock> getTripleBlocks(int graphId);

    /**
     * Getter for the number of graphs.
     * 
     * @return the number of graphs
     */
    public int getNumberOfGraphs();
}
