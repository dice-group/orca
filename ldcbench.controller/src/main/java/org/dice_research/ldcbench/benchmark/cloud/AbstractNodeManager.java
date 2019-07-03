package org.dice_research.ldcbench.benchmark.cloud;

import org.apache.jena.rdf.model.Property;
import org.dice_research.ldcbench.data.NodeMetadata;

public abstract class AbstractNodeManager {
    /**
     * Tells whether nodes of this kind should be included in seed.
     * Typically it is so for nodes which cannot be practically linked from the data,
     * like SPARQL endpoints.
     *
     * @return true if should be in seed.
     */
    public abstract boolean shouldBeInSeed();

    public abstract String[] getDataGeneratorEnvironment(long averageRdfGraphDegree, long triplesPerNode);

    public abstract String getImageName();

    public NodeMetadata getMetadata() {
        return new NodeMetadata();
    }
}
