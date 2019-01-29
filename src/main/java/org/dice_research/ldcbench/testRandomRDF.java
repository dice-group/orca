package org.dice_research.ldcbench;

public class testRandomRDF {
//	private static Random generator ;
		public static void main(String[] args) {
		RandomRDF g = new RandomRDF("Barabasi");        
        int N=40000;
        //int nE=200000;
        int act_nE=g.getBarabasiRDF(N,5.35,123);
        System.out.println("number of edges: " + act_nE);
        //g.print();
        //g.saveToFile(String.format("D:\\RandGj_N%d.txt", N));
	}
	
	}