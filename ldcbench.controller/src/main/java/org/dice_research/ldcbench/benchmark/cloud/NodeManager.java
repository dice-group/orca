package org.dice_research.ldcbench.benchmark.cloud;

import org.dice_research.ldcbench.data.NodeMetadata;

/**
 * Interface of a manager class that gives the benchmark controller the
 * necessary information to create a node of a certain type and supply it with
 * the given data and meta data.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface NodeManager {

    /**
     * Flag indicating whether the node created by this node manager can be used as
     * a hub
     * 
     * @return flag indicating whether the node created by this node manager can be
     *         used as a hub
     */
    public boolean canBeHub();

    /**
     * Returns the Docker image name of the node.
     * 
     * @return the Docker image name of the node.
     */
    public String getNodeImageName();

    /**
     * Returns the environmental values for the generated node.
     * 
     * @return the environmental values for the generated node.
     */
    public String[] getNodeEnvironment();

    /**
     * Returns the Docker image name of the node's data generator.
     * 
     * @return the Docker image name of the node's data generator.
     */
    public String getDataGeneratorImageName();

    /**
     * Returns the environmental values for the generated node's data generator
     * based on the given data.
     * 
     * @param averageRdfGraphDegree the average degree of a node within the
     *                              generated RDF graph
     * @param triplesPerNode        the average number of triples per node
     * @return the environmental values for the generated node's data generator.
     */
    public String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode);

    /**
     * Returns the label of the node type.
     * 
     * @return the label of the node type.
     */
    public abstract String getLabel();

    /**
     * Returns the metadata of this node.
     * 
     * @return the metadata of this node.
     */
    public NodeMetadata getMetadata();

    /**
     * Returns the weight for a link from the given node manager instance to this
     * node manager instance.
     * 
     * @param nodeManager the node manager instance that could create the outgoing link
     * @return the weight of such a link in the range of [0,1]
     */
    public int weightOfLinkFrom(Class<?> nodeManager);
}
