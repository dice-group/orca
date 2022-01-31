package org.dice_research.ldcbench.utils;

import org.apache.jena.rdf.model.Model;
import org.dice_research.ldcbench.generate.seed.DefaultSeedSearcher;
import org.dice_research.ldcbench.generate.seed.SeedSearcher;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.rdf.SimpleRDF2GrphConverter;

public class GraphGeneratorHelper {

    /**
     * Converts the given RDF model into a graph by using the given graph builder
     *
     * @param model   the RDF model that should be converted
     * @param builder the builder to which the result will be written
     */
    public static void convert(Model model, GraphBuilder builder) {
        SimpleRDF2GrphConverter converter = new SimpleRDF2GrphConverter();
        converter.convert(model, builder);
    }

    /**
     * Sets the entrance nodes of the given graph builder.
     *
     * @param builder the graph of which the entrance nodes sould be identified and
     *                set
     */
    public static void setEntranceNodes(GraphBuilder builder) {
        SeedSearcher searcher = new DefaultSeedSearcher();
        builder.setEntranceNodes(searcher.searchSeedNodes(builder));
    }
}
