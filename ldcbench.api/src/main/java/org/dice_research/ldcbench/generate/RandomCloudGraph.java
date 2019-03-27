package org.dice_research.ldcbench.generate;

import org.dice_research.ldcbench.graph.GraphBuilder;

public class RandomCloudGraph implements GraphGenerator{

	//-------------------------------------------------------------------------------------------

	@Override
public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
/*
		generate bi-partite (up to 4 parts(number of types)):1000 hubs, 500 endpoints etc.
		endpoints(same part): connected to each other with power law
		-type0,type1,type2,type3,type4:
		-parts p0,p1,p2,p3 up to p4: each part contains 1 or more types that can connect
		-assign parts to types (Hard code type0 as hubs): start with hubs (sort and FIFS) assign remaining
		-What is d? d is one parameter to the algorithm.
		-Call graphGenerator for each part with number of nodes in that part and d.
		
		*/
		
		ParallelBarabasiRDF rg;
		rg = new ParallelBarabasiRDF("Barabasi Random RDF");
		
	rg.generateGraph(numberOfNodes, avgDegree, seed,builder);
}

@Override
public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {		
	this.generateGraph((int)Math.ceil(numberOfEdges/avgDegree), avgDegree, seed,builder);
}
}
