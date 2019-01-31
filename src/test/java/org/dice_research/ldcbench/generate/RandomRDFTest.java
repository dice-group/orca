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
		rg.generateGraph(N,0.9,123L,g);
		//int act_nE=g.getNumberOfNodes();
		//System.out.println("Number of edges: " + act_nE);
		//assertTrue(act_nE==0);
		//throwCheckedException(true);
	}

    @Test
	public void RealDegree() {
		//int act_nE=g.generate(N,1.5,123,"Barabasi");
		rg.generateGraph(N,1.5,123L,g);
		assertEquals("Number of edges", 60000, g.getNumberOfEdges());
		/*int act_nE=g.getNumberOfEdges();
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==60000);*/
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}

    @Test
	public void IntDegree() {
		//int act_nE=g.generate(N,5,123,"Barabasi");
		rg.generateGraph(5.0,200000,123L,g);
		assertEquals("Number of nodes", 40000, g.getNumberOfNodes());
		assertEquals("Number of edges", 200000, g.getNumberOfEdges());
		
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
}
