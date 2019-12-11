package org.dice_research.ldcbench.tools.analyser;

import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.ldcbench.tools.analyser.LODStatsAnalyser;
import org.junit.Assert;
import org.junit.Test;

public class LODStatsAnalyserTest {

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(), DCAT.downloadURL, "http://example.org/ontology.owl");
        model.add(model.createResource(), DCAT.downloadURL, "http://example.org/ontology#test");
        model.add(model.createResource(), DCAT.downloadURL, "http://example.org/ontology#test2");
        model.add(model.createResource(), DCAT.downloadURL, "http://example.org/sparql");
        model.add(model.createResource(), RDFS.label, "http://example.org/ontology.owl");

        int expectedCounts[] = new int[] { 1, 0, 1, 0, 2 };
        int expectedPLDCounts[] = new int[] { 1, 0, 1, 0, 1 };

        LODStatsAnalyser analyser = new LODStatsAnalyser();
        StreamOps.graphToStream(model.getGraph(), analyser);
        
        Assert.assertArrayEquals(expectedCounts, analyser.getCounts());
        
        @SuppressWarnings("rawtypes")
        Set[] plds = analyser.getPlds();
        int pldCounts[] = new int[plds.length];
        for (int i = 0; i < plds.length; ++i) {
            pldCounts[i] = plds[i].size();
        }
        Assert.assertArrayEquals(expectedPLDCounts, pldCounts);
    }
}
