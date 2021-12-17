package org.dice_research.ldcbench.generate;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.dice_research.ldcbench.graph.GraphBuilder;
import org.dice_research.ldcbench.utils.GraphGeneratorHelper;

import edu.lehigh.swat.bench.uba.Generator;

public class LUBMbasedRDFGenerator implements GraphGenerator {

    private final String FILEEXTENSION = ".owl";
    Model generatedModel = ModelFactory.createDefaultModel();
    private final int AVG_VERTICES_PER_DEPT = 1300;
    private final int AVG_EDGES_PER_DEPT = 4700;
    private final int AVG_VERTICES_PER_UNIVERSITY = 15 * AVG_VERTICES_PER_DEPT;
    private final int AVG_EDGES_PER_UNIVERSITY = 15 * AVG_EDGES_PER_DEPT;    

    protected Model createModel(int numberOfUni, int numberOfFilesToParse, long seed) {
        Generator lubmGenerator = new Generator();
        lubmGenerator.start(numberOfUni, 0, (int) seed, false, "");
        File rdfFilesDest = new File(System.getProperty("user.dir"));
        File[] rdfFiles = rdfFilesDest.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FILEEXTENSION);
            }
        });
        Model lubmModel = ModelFactory.createDefaultModel();
        for (int i = 0; i < numberOfFilesToParse; i++) {
            Model model = ModelFactory.createDefaultModel();
            FileManager.get().readModel(model, rdfFiles[i].getPath());
            lubmModel.add(model);
        }
        return lubmModel;
    }

    @Override
    public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
        GraphGeneratorHelper.convert(createModel(getNumberOfUniFromNumberOfNodes(numberOfNodes),
                getNumberOfFilesToParseFromNumberOfNodes(numberOfNodes), seed), builder);
        GraphGeneratorHelper.setEntranceNodes(builder);
    }

    @Override
    public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {
        GraphGeneratorHelper.convert(createModel(getNumberOfUniFromNumberOfEdges(numberOfEdges),
                getNumberOfFilesToParseFromNumberOfEdges(numberOfEdges), seed), builder);
        GraphGeneratorHelper.setEntranceNodes(builder);
    }
    
    private int getNumberOfUniFromNumberOfNodes(int numberOfNodes) {
        return (int) Math.ceil(numberOfNodes / (float) AVG_VERTICES_PER_UNIVERSITY);
    }
    
    private int getNumberOfUniFromNumberOfEdges(int numberOfEdges) {
        return (int) Math.ceil(numberOfEdges / (float) AVG_EDGES_PER_UNIVERSITY);
    }
    
    private int getNumberOfFilesToParseFromNumberOfNodes(int numberOfNodes) {
        return (int) Math.ceil(numberOfNodes / (float) AVG_VERTICES_PER_DEPT);
    }
    
    private int getNumberOfFilesToParseFromNumberOfEdges(int numberOfEdges) {
        return (int) Math.ceil(numberOfEdges / (float) AVG_EDGES_PER_DEPT);
    } 

}
