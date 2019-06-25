package org.dice_research.ldcbench.generate;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.dice_research.ldcbench.generate.RandomRDF;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;

public class RandomG2File {
	public static void main(String[] args){
		int N;
		double degree;
/*		RandomRDF rg;
		rg = new RandomRDF("Barabasi Random RDF");*/
		ParallelBarabasiRDF rg;
		GrphBasedGraph g;
    	g = new GrphBasedGraph();
		rg = new ParallelBarabasiRDF("Barabasi Random RDF");
		//DBpedia en scale
//		N = 1000000;    	degree=117.5;
//		N =43497; degree = 4.096;
		N=40000;degree = 20;


		//Yeast
//		N=82481;
//		degree=223684.0/N;

		//Toy
//		N=100;		degree=2.7;

		rg.generateGraph(N,degree,123L,g);
		//g.getNumberOfNodes();

		//public void saveToFile(String fname) {
		String fname=String.format("D:\\RandParSWDF_inDeg_N%s_%.1f_123.txt",N,degree);
			try {
				PrintWriter oout = new PrintWriter(new FileWriter(fname));
				for (int i = 0; i < N; i++) {
					int[] targets=g.outgoingEdgeTargets(i);
					for(int dist:targets)
						oout.println((i+1) + " " + (dist+1));
				}

				oout.close();
			} catch (IOException e) {
				System.out.println("Failed to open output file. ");
				e.printStackTrace();
			}
		//}
	}
}
