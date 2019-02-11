package org.dice_research.ldcbench.generate;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.dice_research.ldcbench.generate.RandomRDF;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

public class RandomG2File {
	public static void main(String[] args){
		int N;
		RandomRDF rg;
		GrphBasedGraph g;
    	g = new GrphBasedGraph();
		rg = new RandomRDF("Barabasi Random RDF");
		N = 1000000;		
		double degree=117.5;
		rg.generateGraph(N,degree,123L,g);
		//g.getNumberOfNodes();
		
		//public void saveToFile(String fname) {
		String fname=String.format("D:\\RandGj_inDeg_N%s_%.1f_123.txt",N,degree);
		System.out.println("Saving to file ..");
			try {
				PrintWriter oout = new PrintWriter(new FileWriter(fname));
				for (int i = 0; i < N; i++) {
					int[] targets=g.outgoingEdgeTargets(i);
					for(int dist:targets)
						oout.println((i+1) + " " + (dist+1));
				}
					
				oout.close();
				System.out.print(" Completed.");
			} catch (IOException e) {
				System.out.println("Failed to open output file. ");
				e.printStackTrace();
			}
		//}
	}
}
