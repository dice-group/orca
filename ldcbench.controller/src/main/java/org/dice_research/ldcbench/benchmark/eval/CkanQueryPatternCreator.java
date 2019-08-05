package org.dice_research.ldcbench.benchmark.eval;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.ldcbench.rdf.SimpleTripleCreator;

public class CkanQueryPatternCreator implements QueryPatternCreator {
    /**
     * Triple creator used to construct nodes.
     */
    protected final SimpleTripleCreator tripleCreator;

    /**
     * Constructor.
     */
    protected CkanQueryPatternCreator(int graphId, String[] resourceUriTemplates, String[] accessUriTemplates) {
        tripleCreator = new SimpleTripleCreator(graphId, resourceUriTemplates, accessUriTemplates);
    }

    /**
     * Creates triple pattern expected to be in CKAN for the specified edge
     * which links to another node.
     */
    @Override
    public ElementTriplesBlock create(int sourceId, int propertyId, int targetId, int targetExtId, int targetExtGraphId) {
        ElementTriplesBlock pattern = new ElementTriplesBlock();
        Node dataset = NodeFactory.createVariable("dataset");
        Node distribution = NodeFactory.createVariable("resource");
        Node target = tripleCreator.createNode(targetId, targetExtId, targetExtGraphId, false);
        pattern.addTriple(new Triple(dataset, RDF.type.asNode(), DCAT.Dataset.asNode()));
        pattern.addTriple(new Triple(dataset, DCAT.distribution.asNode(), distribution));
        pattern.addTriple(new Triple(dataset, DCTerms.title.asNode(), NodeFactory.createLiteral("Dataset " + target.getURI())));
        pattern.addTriple(new Triple(distribution, RDF.type.asNode(), DCAT.Distribution.asNode()));
        pattern.addTriple(new Triple(distribution, DCAT.accessURL.asNode(), target));
        return pattern;
    }
}
