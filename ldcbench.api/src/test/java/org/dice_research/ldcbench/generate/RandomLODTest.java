package org.dice_research.ldcbench.generate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.dice_research.ldcbench.generate.RandomCloudGraph;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
public class RandomLODTest {
	int N=1069;
	RandomCloudGraph rg;
	GrphBasedGraph g;
    int[] typecounts= {500,400,100,69};
    int[][] typeconn= {{1,1,0,0},{0,1,1,0},{0,0,1,1},{1,1,1,1}};
	//	private static Random generator ;
    @Before
	public void setUp() {
    	g = new GrphBasedGraph();
		rg = new RandomCloudGraph("Barabasi Random RDF",typecounts,100,typeconn);		
	}

    @Test(expected = IllegalArgumentException.class)
	public void ValidDegree() throws IllegalArgumentException {
		rg.generateGraph(N,0.9,123L,g);
	}

    @Test(expected = IllegalArgumentException.class)
	public void maxDegree() throws IllegalArgumentException {
		rg.generateGraph(4.0,2,123L,g);
	}
}
