package org.dice_research.ldcbench.rdf;

import org.apache.jena.graph.Triple;

/**
 * An interface for a class which can generate a triple based on Ids that it got
 * from a graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface TripleCreator {

    public Triple createTriple(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId);

    /**
     * Create a triple where the Object might be a BlankNode or a literal
     */
    public Triple createTriple(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId,
            RDFNodeType nodeType);
}
