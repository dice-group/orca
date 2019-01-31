//package org.dice_research.ldcbench;
package org.dice_research.ldcbench.generate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RandomRDFTest {
	int N;
	RandomRDF g;

	//	private static Random generator ;
    @Before
	public void setUp() {
		g = new RandomRDF("Barabasi Random RDF");
		N = 40000;
	}

    @Test
	public void ValidDegree() {
		int act_nE=g.generate(N,0.9,123,"Barabasi");
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==0);
	}

    @Test
	public void RealDegree() {
		int act_nE=g.generate(N,1.5,123,"Barabasi");
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==60000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}

    @Test
	public void IntDegree() {
		int act_nE=g.generate(N,5,123,"Barabasi");
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==200000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
}
