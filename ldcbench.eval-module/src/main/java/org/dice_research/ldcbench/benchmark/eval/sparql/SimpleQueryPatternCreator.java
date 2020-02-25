package org.dice_research.ldcbench.benchmark.eval.sparql;

import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.rdf.SimpleTripleCreator;

public class SimpleQueryPatternCreator implements QueryPatternCreator {
    /**
     * Triple creator used to construct triples.
     */
    protected final SimpleTripleCreator tripleCreator;

    /**
     * Constructor.
     */
    public SimpleQueryPatternCreator(int graphId, String[] resourceUriTemplates, String[] accessUriTemplates) {
        tripleCreator = new SimpleTripleCreator(graphId, resourceUriTemplates, accessUriTemplates);
    }

    /**
     * Creates a corresponding triple for the specified graph edge.
     */
    @Override
    public ElementTriplesBlock create(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId) {
        ElementTriplesBlock pattern = new ElementTriplesBlock();
        pattern.addTriple(tripleCreator.createTriple(sourceId, propertyId, targetId, targetExtId, targetExtGraphId));
        return pattern;
    }
}
