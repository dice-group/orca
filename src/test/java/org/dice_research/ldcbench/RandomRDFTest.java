package org.dice_research.ldcbench;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RandomRDFTest {
	int N;
	RandomRDF g;

	//	private static Random generator ;
    @Before
	public void setUp() {
		g = new RandomRDF("Barabasi");
		N = 40000;
	}

    @Test
	public void testValidDegree() {
		int act_nE=g.getBarabasiRDF(N,0.9,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==0);
	}

    @Test
	public void testRealDegree() {
		int act_nE=g.getBarabasiRDF(N,1.5,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==60000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}

    @Test
	public void testIntDegree() {
		int act_nE=g.getBarabasiRDF(N,5,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==200000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
}
