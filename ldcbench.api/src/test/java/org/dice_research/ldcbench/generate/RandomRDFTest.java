//package org.dice_research.ldcbench;
package org.dice_research.ldcbench.generate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.dice_research.ldcbench.generate.RandomRDF;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

public class RandomRDFTest {
	int N;
	RandomRDF rg;
	GrphBasedGraph g;

	//	private static Random generator ;
    @Before
	public void setUp() {
    	g = new GrphBasedGraph();
		rg = new RandomRDF("Barabasi Random RDF");
		N = 40000;
		
	}

    @Test(expected = IllegalArgumentException.class)
	public void ValidDegree() throws IllegalArgumentException {
		rg.generateGraph(N,-0.1,123L,g);
	}

    @Test(expected = IllegalArgumentException.class)
	public void maxDegree() throws IllegalArgumentException {
		rg.generateGraph(4.0,2,123L,g);
	}

    @Test
	public void RealDegree() {
		rg.generateGraph(N,1.5,123L,g);
		assertEquals("Number of edges", 60000, g.getNumberOfEdges());
	}

    @Test
	public void IntDegree() {
		rg.generateGraph(5.0,200000,123L,g);
		assertEquals("Number of nodes", 40000, g.getNumberOfNodes());
		assertEquals("Number of edges", 200000, g.getNumberOfEdges());
		
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
    
}
