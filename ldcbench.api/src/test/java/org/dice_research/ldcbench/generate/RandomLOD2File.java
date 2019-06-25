package org.dice_research.ldcbench.generate;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.dice_research.ldcbench.generate.RandomCloudGraph;

public class RandomLOD2File {
	public static void main(String[] args){
	RandomCloudGraph rg;
	GrphBasedGraph g;

//	int N=28;
//	int[] typecounts= {14,8,6};
//    int[][] typeconn= {{1,1,0},{0,1,1},{1,1,1}};
//    double degree=46.0/N;

    int N=1096;
    int[] typecounts= {500,400,100,96};
    int[][] typeconn= {{1,1,0,0},{0,1,1,0},{0,0,1,1},{1,1,1,1}};
    double degree=15655.0/N;

    g = new GrphBasedGraph();
	rg = new RandomCloudGraph("RandomLOD",typecounts,100,typeconn);
	rg.generateGraph(N,degree,123L,g);

	String fname=String.format("D:\\RandLOD_inDeg_N%s_%.1f_123.txt",N,degree);
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
		//save assigned types
		int[] nodeTypes=rg.getNodeTypes();
		fname=String.format("D:\\RandLODnType_inDeg_N%s_%.1f_123.txt",N,degree);
			try {
				PrintWriter oout = new PrintWriter(new FileWriter(fname));
				for (int i = 0; i < N; i++) {
					oout.println(nodeTypes[i]);
				}

				oout.close();
			} catch (IOException e) {
				System.out.println("Failed to open output file. ");
				e.printStackTrace();
			}
}

}
