package org.dice_research.ldcbench.generate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice_research.ldcbench.generate.seed.DefaultSeedSearcher;
import org.dice_research.ldcbench.generate.seed.SeedSearcher;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.graph.rdf.SimpleRDF2GrphConverter;
import org.dice_research.ldcbench.utils.GraphGeneratorHelper;

/**
 * A simple graph generator that loads the graph from a given RDF file and
 * converts it into a graph object.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class FileBasedGraphGenerator implements GraphGenerator {

    protected String modelLocation;
    protected String modelBase;
    protected String modelLang;

    /**
     * Constructor.
     * 
     * @param modelLocation the location of the RDF file
     * @param modelBase     the base IRI of the RDF model
     * @param modelLang     the serialization of the RDF model
     */
    public FileBasedGraphGenerator(String modelLocation, String modelBase, String modelLang) {
        this.modelLocation = modelLocation;
        this.modelBase = modelBase;
        this.modelLang = modelLang;
    }

    @Override
    public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
        generateGraph(builder);
    }

    @Override
    public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {
        generateGraph(builder);
    }

    /**
     * Internal method that generates a graph by loading it from a file and writing
     * the converted graph (and the array of entrance nodes) to the given graph
     * builder.
     * 
     * @param builder the builder that should be used to write the graph data
     */
    protected void generateGraph(GraphBuilder builder) {
        GraphGeneratorHelper.convert(loadRDFModel(), builder);
        GraphGeneratorHelper.setEntranceNodes(builder);
    }

    protected Model loadRDFModel() {
        Model model = ModelFactory.createDefaultModel();
        model.read(modelLocation, modelBase, modelLang);
        return model;
    }
}
