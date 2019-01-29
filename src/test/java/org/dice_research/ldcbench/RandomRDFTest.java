package org.dice_research.ldcbench;

import junit.framework.TestCase;

public class RandomRDFTest extends TestCase {
	int N;
	RandomRDF g;
	
	//	private static Random generator ;
	protected void setUp() {
		g = new RandomRDF("Barabasi");        
		N = 40000;
	}

	public void testValidDegree() {
		int act_nE=g.getBarabasiRDF(N,0.9,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==0);
	}
	
	public void testRealDegree() {
		int act_nE=g.getBarabasiRDF(N,1.5,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==60000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
	
	public void testIntDegree() {
		int act_nE=g.getBarabasiRDF(N,5,123);
		System.out.println("Number of edges: " + act_nE);
		assertTrue(act_nE==200000);
		//g.print();
		//g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
}