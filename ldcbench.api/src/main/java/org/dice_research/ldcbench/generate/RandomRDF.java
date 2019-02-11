//28/1/2019
//ToDo: add one method to generate graphs that takes the algorithm as a parameter. Done!

//package org.dice_research.ldcbench;
package org.dice_research.ldcbench.generate;

import java.util.Arrays;
import java.util.Random;

import org.dice_research.ldcbench.graph.GraphBuilder;

public class RandomRDF implements GraphGenerator{
	protected Random generator;
	public String name;
	
	public RandomRDF(String gname) {
		name = gname;
	}
	
/**
 * Samples numbers from 1 to n; m times without replacement using weights in wt
 * @param n number of values to choose from.
 * @param m number of values to choose.
 * @param wt the weights for the values in index from 1 to n (0 is ignored).
 * @return the number of edges of the generated graph; 0 if failed.
 */
	protected int[] weightedSampleWithoutReplacement(int n, int m, int[] wt) {
		
		int[] Res = new int[m];
		int[] aw = new int[n + 1];// accumulated weights
		aw[0] = 0;

		int last = aw.length - 1;
		int rand;
		int i;

		for (i = 1; i <= n; i++) {
			aw[i] = aw[i - 1] + wt[i];
		}

		for (int j = 0; j < m; j++) {
			rand = 1 + generator.nextInt(aw[last]);

			// find interval
			for (i = 1; i < n; i++) {
				if (rand <= aw[i]) {
					Res[j] = i;
					break;
				}
			}
			if (Res[j] == 0)
				Res[j] = n;

			// update probabilities to exclude taken
			if (j < m - 1) {
				int Difference = wt[Res[j]];
				for (i = Res[j]; i < aw.length; i++) {
					aw[i] -= Difference;
				}
			}
		}

		return (Res);
	}
	
	/*
	 * generate a random RDF graph using algorithm  
	 * 
	 * @param N the number of nodes
	 * @param degree the average degree of the nodes in the graph
	 * @param seed the random seed to be able to reproduce the results.
	 * @param algorithm the algorithm used to generate the graph values ("Barabasi")
	 * @return the number of edges if successful and zero otherwise
	 *
		protected int generate(int N, double degree, long seed,String algorithm) {
			if(algorithm.equals("Barabasi")) {
				return(getBarabasiRDF( N,  degree,  seed));
			}else {
				System.out.println("Unknown algorithm: "+algorithm);
				return(0);
			}
		}
		*/
	/**
	 * generate a random RDF graph using Barabasi algorithm and 
	 * inverting some edges to be sure that the graph is connected (reachable from a single source).
	 * @param N the number of nodes
	 * @param degree the average degree of the nodes in the graph
	 * @param seed the random seed to be able to reproduce the results.
	 * @return the number of edges if successful and zero otherwise
	 */
		protected void getBarabasiRDF(int N, double degree, long seed, GraphBuilder builder) {
			/* nodes are numbered from 1 to N */
			int indexToEdgeList = 0;// index to edge list
			//Random generator;
			// RDF_graph g=new RDF_graph();
			int nE=(int) Math.ceil(N*degree);
			int[] subj = new int[nE];
			int[] obj = new int[nE];
			int[] inDeg = new int[N + 1];
			Arrays.fill(inDeg, 1);
			inDeg[0] = 0;// not used
			long t0 = System.currentTimeMillis();
			long ti0 = System.currentTimeMillis();
			
			if (degree < 1) {
				throw new IllegalArgumentException("Degree must be more than 1.");
			}
			
			if (degree > N) {
				throw new IllegalArgumentException("Degree can NOT be more than the number of nodes.");
			}
			
			generator = new Random(seed);// seed
			int m = (int) Math.floor(degree);// average degree of graph
			if(m >1 ) {
				subj[indexToEdgeList] = 1;
				obj[indexToEdgeList] = 2;// first edge
				indexToEdgeList++;
				inDeg[2] = 2;
			}
						
			// initial part
			if (m > 2) {
				for (int i = 3; i <= m; i++) {
					int[] tmp = weightedSampleWithoutReplacement(i - 1, 2, inDeg);// new links
					boolean randIndex = generator.nextBoolean();// runif(1)
					int vto;
					if (randIndex) {
						vto = 0;
					} else {
						vto = 1;
					}
					subj[indexToEdgeList] = i;
					obj[indexToEdgeList] = tmp[1 - vto];
					indexToEdgeList++;
					subj[indexToEdgeList] = tmp[vto];
					obj[indexToEdgeList] = i;
					indexToEdgeList++;// inverted link randomly
				}
			}
			
			System.out.println(String.format("init graph: m0=%d, nE=%d", m, indexToEdgeList ));
			
			int P1 = (m + 1)*(N-m) - (nE - indexToEdgeList) + m ;
			
			System.out.println("Adding other nodes: " + (N - m) +" P1:" + P1);			
			
			double biasedCoin=((m/2.0-1)/(m-1));
			for (int i = m + 1; i <= N; i++) {

				int[] tmp = weightedSampleWithoutReplacement((i - 1), m, inDeg);// #new links

				 //in- link
				int vin_ix = (int) Math.floor(generator.nextDouble() * m);
				if (vin_ix == m)
					vin_ix--;
               /*
				for (int k = 0; k < m; k++) {
					if (k != vin_ix) {
						inDeg[tmp[k]] = inDeg[tmp[k]] + 1;
						subj[indexToEdgeList] = i;
						obj[indexToEdgeList] = tmp[k];						
					} else {// inverted link
						inDeg[i] = inDeg[i] + 1;
						subj[indexToEdgeList] = tmp[k];
						obj[indexToEdgeList] = i;						
					}
					indexToEdgeList++;
				} */
				/*
				 * 8/2/2019: choose randomly the number of in-links [1,m],
				 * 	 1. select randomly one link to be in-link then 
				 *   2. toss a biased coin ((m/2)-1 inlink,(m/2) outlink)
				 */
				for (int k = 0; k < m; k++) {					
					if (k != vin_ix && (generator.nextDouble() > biasedCoin) ) {						
						inDeg[tmp[k]] = inDeg[tmp[k]] + 1;
						subj[indexToEdgeList] = i;
						obj[indexToEdgeList] = tmp[k];						
					} else {// inverted link
						inDeg[i] = inDeg[i] + 1;
						subj[indexToEdgeList] = tmp[k];
						obj[indexToEdgeList] = i;						
					}
					indexToEdgeList++;
				}
				if(i==P1) { 
					    m = m + 1;//second part
				        biasedCoin=((m/2.0-1)/(m-1));
				}
                
				if (i % 10000 == 0) {
					long ti1;
					ti1= System.currentTimeMillis();
					System.out.println(String.format("processed nodes: %d, time: %.1f", i,(ti1-ti0)/1000.0));
					ti0= System.currentTimeMillis();				
				}
			}
			
			long t_barabasi = System.currentTimeMillis();
			System.out.println("time Barabsi =" + ((t_barabasi-t0)/1000.0));
			
			System.out.println("nE =" + nE +" indexToEdgeList:" + indexToEdgeList);
			
			int nEdges = indexToEdgeList;
			int[] idRange=builder.addNodes(N);//Range
			for(int i=0; i < nEdges; i++) {
				if(!builder.addEdge(subj[i]-1 + idRange[0], obj[i]-1+idRange[0], 0)) {
					System.out.println("Failed to add edge : i="+i+" subj="+subj[i]+" idRange[0]=" + idRange[0]+" obj[i]="+ obj[i]);
				};
			}
			long t_gbuilder = System.currentTimeMillis();
			System.out.println("time Barabsi =" + ((t_barabasi-t0)/1000.0) +" time formatting = " + ((t_gbuilder-t_barabasi)/1000.0) + " sec");			
		}

	/*public void print() {
		System.out.println("nNodes:" + nNodes + " nEdges:" + nEdges);
		for (int i = 0; i < nEdges; i++)
			System.out.println(subject[i] + "->" + object[i]);
	}

	*/

	@Override
	public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
		
		//this.generate(numberOfNodes, avgDegree, seed,"Barabasi");//String algorithm	
		this.getBarabasiRDF(numberOfNodes, avgDegree, seed,builder);//String algorithm	
	}

	@Override
	public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {		
		//this.generate((int)Math.floor(numberOfEdges*avgDegree), avgDegree, seed,"Barabasi");
		this.getBarabasiRDF((int)Math.ceil(numberOfEdges/avgDegree), avgDegree, seed,builder);
	}
}
