package org.dice_research.ldcbench.benchmark.node;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.dice_research.ldcbench.vocab.LDCBench;
import org.hobbit.utils.rdf.RdfHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeSizeDeterminerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSizeDeterminerFactory.class);

    /**
     * Creates a {@link NodeSizeDeterminer} instance based on the configuration in
     * the given RDF model. If the determiner uses a seed, the given seed will be
     * used.
     * 
     * @param parameterModel
     *            RDF model containing the type and necessary configuration of the
     *            {@link NodeSizeDeterminer}
     * @param seed
     *            the seed used by {@link NodeSizeDeterminer} instances that are
     *            relying on a random number generator.
     * @return the created {@link NodeSizeDeterminer}
     */
    public static NodeSizeDeterminer create(Model parameterModel, long seed) {
        int triplesPerNode = RdfHelper.getLiteral(parameterModel, null, LDCBench.averageTriplesPerNode).getInt();
        Resource method = RdfHelper.getObjectResource(parameterModel, null, LDCBench.nodeSizeDeterminer);

        NodeSizeDeterminer determiner = null;
        if (LDCBench.ExponentialDistNodeSize.equals(method)) {
            determiner = new ExponentialDistBasedNodeSizeDeterminer(triplesPerNode, seed);
        } else {
            // use the static approach
            determiner = new StaticNodeSizeDeterminer(triplesPerNode);
            // check whether there was an issue with the configuration
            if (!LDCBench.StaticNodeSize.equals(method)) {
                LOGGER.error(
                        "Couldn't find the method to determine the single node sizes named {}. Using the static node size method.",
                        method.getURI());
            } else if (method == null) {
                LOGGER.error(
                        "Couldn't load the method to determine the single node sizes. Using the static node size method.");
            }
        }
        return determiner;
    }

}
