package org.dice_research.ldcbench.generate;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.utils.GraphGeneratorHelper;

import edu.lehigh.swat.bench.uba.Generator;

public class LUBMbasedRdfGenerator implements GraphGenerator {

    private final String FILEEXTENSION = ".owl";
    Model generatedModel = ModelFactory.createDefaultModel();

    protected Model createModel(int numberOfNumber, long seed) {
        Generator lubmGenerator = new Generator();
        //ajouter une methode pour avoir le nombre d universite
        lubmGenerator.start(numberOfNumber, 0, (int) seed, false, "");
        File rdfFilesDest = new File(System.getProperty("user.dir"));
        File[] rdfFiles = rdfFilesDest.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FILEEXTENSION);
            }
        });
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, rdfFiles[0].getPath());
        return model;
    }

    @Override
    public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
        GraphGeneratorHelper.convert(createModel(numberOfNodes, seed), builder);
        GraphGeneratorHelper.setEntranceNodes(builder);
    }

    @Override
    public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {
        // TODO Auto-generated method stub
    }

}
