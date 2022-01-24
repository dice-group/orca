package org.dice_research.ldcbench.benchmark.eval.sparql;

import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.rdf.RDFNodeType;

/**
 * Interface for a class which can build a query pattern used in evaluation.
 */
public interface QueryPatternCreator {

    /**
     * Returns a query pattern to validate.
     *
     * @return the query pattern
     */
    public ElementTriplesBlock create(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId);

    /**
     * Returns a query pattern to validate.
     *
     * @return the query pattern
     */
    public ElementTriplesBlock create(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId, RDFNodeType targetNodeType);
}
