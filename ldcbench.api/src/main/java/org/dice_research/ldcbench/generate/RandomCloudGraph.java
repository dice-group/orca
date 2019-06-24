package org.dice_research.ldcbench.generate;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.LinkedList;
import java.util.Queue;

import org.dice_research.ldcbench.graph.GraphBuilder;

public class RandomCloudGraph implements GraphGenerator{
//	protected Random generator;

    public String name;
	protected int[] nodetypes;
	protected boolean[] ishub;
	protected int[][] typeconnectivity;
	protected int hcount;

	public RandomCloudGraph(String gname,int[] nt,boolean[] ih, int[][] tc) {
		name = gname;
		nodetypes=nt;
		ishub=ih;
		typeconnectivity=tc;
	}

	public RandomCloudGraph(String gname,int[] typecounts,int hcount, int[][] tc) {
		name = gname;
		nodetypes=getNodeSequence(typecounts);
		//ishub=ih;
		typeconnectivity=tc;
		this.hcount=hcount;
	}

	public boolean[] getHubs() {
		return(ishub);
	}

	public int[] getNodeTypes() {
		return(nodetypes);
	}
	//-------------------------------------------------------------------------------------------

	@Override
public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
	/*
	 * rules of which node types are allowed to link to which (maybe based on weights), e.g., {{1,1,0},{0,1,1},{1,1,1}}
        which means that the following links are allowed: 0 --> 0, 0 --> 1, 1 --> 1, 1 --> 2, 2 --> 0, 2 --> 1, 2 --> 2
        while 0 --> 2 and 1 --> 0 are not allowed
	 */

		getRandomLOD(numberOfNodes,avgDegree,seed,0.7,builder);
}

@Override
public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {
	this.generateGraph((int)Math.ceil(numberOfEdges/avgDegree), avgDegree, seed,builder);
}

//--------------------------------------------------------------------------------------------

protected void getRandomLOD(int N, double degree, long seed, double outlinkspct,GraphBuilder builder) {
	/* nodes are numbered from 1 to N */
	/* TreeSet is faster practically than HashSet */

	if (degree < 1) {
		throw new IllegalArgumentException("Degree must be more than 1.");
	}

	if (degree > (N-1)) {// max links created at any step is N-1
		throw new IllegalArgumentException("Degree can NOT be more than (N-1).");
	}

	int indexToEdgeList ;// index to edge list
	long t0 = System.currentTimeMillis();
	long ti0 = System.currentTimeMillis();

	int nE=(int) Math.ceil(N*degree);
	int[] subj = new int[nE];
	int[] obj = new int[nE];
	int[] inDeg = new int[N + 1];
	int[] typewt = new int[N + 1];
	Arrays.fill(inDeg, 1);
	inDeg[0] = 0;// reserved
	int m = (int) Math.floor(degree);// average degree of graph

	Random generator=new Random(seed);
	/* -init:until the number of nodes to connect to for each type is at least d (check if <=d =>conn2all),
	-source node should be of type able to connect to all others
	-in-link should be from type able to connect to current node:if all links are not eligible
	-- better make in two steps: select out links from possible destinations and select one coming from possible sources
	*/
	/*
	 * start by one node of each type with all possible connections (root able to connect to all)
	 * --- or mst (min sp tree)
	 */
	//indexToEdgeList = getInitGraph(N,degree,seed,subj, obj,inDeg,m,generator);
	int il=0;
	/*for(int i=0; i < typeconnectivity.length;i++) {// start by all possible connections
		for(int j = 0; j < typeconnectivity.length; j++) {
			if(i==j) continue;
			if(typeconnectivity[i][j]==1) {
				subj[il] = i+1; obj[il] = j+1;
				il++;
			}
		}
	}*/
	// only one out connection
	for(int i=0; i < typeconnectivity.length;i++) {// start by all possible connections
		for(int j = 0; j < typeconnectivity.length; j++) {
			if(i==j) continue;
			if(typeconnectivity[nodetypes[i]][nodetypes[j]]==1) {
				subj[il] = i+1; obj[il] = j+1;
				il++;
				break;
			}
		}
	}
	// type of node
	int ctype ;
	int ndests,nsrcs;

	//--------------------------------------
	indexToEdgeList=il;
	System.out.println(String.format("init graph: m0=%d, nE=%d", m, indexToEdgeList ));

	int P1 = (m + 1)*(N-m) - (nE - indexToEdgeList) + m ;
	if (P1 <= m ) P1 = m + 1;
	System.out.println("Adding other nodes: " + (N - typeconnectivity.length) +" P1:" + P1);
	//
	for(int node=typeconnectivity.length+1;node<=N;node++) {//next node

		if(node==P1) {
			m = m + 1;//second part
		}

		int m1,mi,mo;//number of inlinks and number of out links mi+mo=m1
		m1= 1+ generator.nextInt(2*m);// to use uniform distribution from 1 to 2m
		if(m1 >= node) m1=node-1;
		if(m1 > 2*m) m1=2*m;

		if((N-node)>=(nE-indexToEdgeList-m1+1)) m1=1;// to have the same number of nodes

		if(m1==1) {
			if(generator.nextDouble()> outlinkspct) { mo=0;}
			else {mo=1;}
		}else{
			mo = (int) Math.round(m1 * outlinkspct);
		}
		mi = m1 - mo;
		System.out.println("m1="+m1+" mi="+mi+" mo="+mo);

		ctype = nodetypes[node-1];
		//get possible out connections
		if(mo > 0) {
			ndests=0;
			for(int i=1; i < node; i++) {//only use weights of nodes that accepts connections from current node.
				if(typeconnectivity[ctype][nodetypes[i-1]]==1) {
					typewt[i]=inDeg[i];
					ndests++;
				}
				else typewt[i]=0;
			}
			System.out.println("node=" + node + " ctype=" + ctype + " ndests=" + ndests);
			int[] outlinks;
			if(ndests > mo) {//sample out-links
				outlinks = weightedSampleWithoutReplacement(node-1, mo, typewt, generator);

				for (int k = 0; k < mo; k++) {
						inDeg[outlinks[k]] = inDeg[outlinks[k]] + 1;
						subj[indexToEdgeList] = node;
						obj[indexToEdgeList] = outlinks[k];
					    indexToEdgeList++;
					    if(indexToEdgeList >= nE) break;
				}
			}else {//connect to all if count less than m
				for(int i=1; i < node; i++) {//only use weights of nodes that accepts connections from current node.
					if(typewt[i] > 0) {
						inDeg[i] = inDeg[i] + 1;
						subj[indexToEdgeList] = node;
						obj[indexToEdgeList] = i;
					    indexToEdgeList++;
					    if(indexToEdgeList >= nE) break;
					}
				}
			}
		}
		//---- in-link: there can be in and out links between two nodes[DBpedia&Revyu]
		if(indexToEdgeList >= nE) break;
		if( mi > 0 ) {
			nsrcs=0;
			for(int i=1; i < node; i++) {//only use weights of nodes that accepts connections from current node.
				if(typeconnectivity[nodetypes[i-1]][ctype]==1) {
					typewt[i]=inDeg[i];
					nsrcs++;
				}
				else typewt[i]=0;
			}
			System.out.println("node="+node+" ctype="+ctype+" nsrcs="+nsrcs+" nEdg:"+indexToEdgeList);
			int[] inlinks;
			if(nsrcs > mi) {//sample in-links
				inlinks = weightedSampleWithoutReplacement(node-1, mi, typewt, generator);
				inDeg[node] = inDeg[node] + mi;
				for (int k = 0; k < mi; k++) {
						subj[indexToEdgeList] = inlinks[k];
						obj[indexToEdgeList] = 	 node;
					    indexToEdgeList++;
					    if(indexToEdgeList >= nE) break;
				}
			}else {//connect to all if count less than m
				inDeg[node] = inDeg[node] + nsrcs;
				for(int i=1; i < node; i++) {//only use weights of nodes that accepts connections from current node.
					if(typewt[i] > 0) {
	//					inDeg[node] = inDeg[node] + 1;
						subj[indexToEdgeList] = i;
						obj[indexToEdgeList] = node;
					    indexToEdgeList++;
					    if(indexToEdgeList >= nE) break;
					}
				}
			}
		}

//		The dataset currently contains 1,239 datasets with 16,147 links (as of March 2019): d~=15

		if (node % 10000 == 0) {
			long ti1;
			ti1= System.currentTimeMillis();
			System.out.println(String.format("processed nodes: %d, time: %.1f", node,(ti1-ti0)/1000.0));
			ti0= System.currentTimeMillis();
		}
	if(indexToEdgeList >= nE) break;
	}
	System.out.println("nEdges="+indexToEdgeList);
	//Grph structure ----------------------------------------------
	int nEdges = indexToEdgeList;
	int[] idRange=builder.addNodes(N);//Range
	for(int i=0; i < nEdges; i++) {
		System.out.println(i+"    "+(idRange[0]+subj[i]-1) +" -> "+ (obj[i]-1+idRange[0]));
		if(!builder.addEdge(subj[i]-1 + idRange[0], obj[i]-1+idRange[0], 0)) {
			System.out.println("Failed to add edge : i="+i+" subj="+subj[i]+" idRange[0]=" + idRange[0]+" obj[i]="+ obj[i]);
		};
	}

	int[] entranceNodes;
	entranceNodes = findEnteranceNodes(builder);
	builder.setEntranceNodes(entranceNodes);

}
//----------------------------------------------------------

public int[] findEnteranceNodes(GraphBuilder g) {
	int N=g.getNumberOfNodes();
	boolean[] visited=new boolean[N],isEnterance=new boolean[N];
	Arrays.fill(visited, false);
	Arrays.fill(isEnterance, false);
	int cntENodes=0;

	Queue<Integer> processQ=new LinkedList<>();
	for(int i = 0 ; i < N; i++) {
		int[] sources=g.incomingEdgeSources(i);
		if(sources.length==0) {
			   System.out.println("****************new enterance [ "+ i + " ]************");
			processQ.add(i);
			visited[i] = isEnterance[i] = true;
			cntENodes++;
		}
	}

	boolean finished=false;
	int node ;
	if(cntENodes==0) {
		node = 0;//to be processed
		processQ.add(0);
		visited[0] = isEnterance[0] = true;
		cntENodes++;
	}

	while(!finished) {
		node=processQ.poll();
		int[] targets=g.outgoingEdgeTargets(node);
		//processed[node]=true;
		System.out.println("=============Processed========[ "+node+" ]==============");
		for(int dist:targets) {
			if(!visited[dist]) {
				System.out.println("visited node:"+dist);
				visited[dist]=true;
				processQ.add(dist);
			}
		}

		if(processQ.isEmpty()) {
			finished=true;
			for(int j=0; j < N; j++) {
			   if(!visited[j]) {
				   System.out.println("****************new enterance [ "+ j + " ]************");
				   isEnterance[j]=true;
				   visited[j]=true;
				   System.out.println("visited node:"+ j);
				   finished=false;
				   cntENodes++;

				   processQ.add(j);
				   break;
			   }
			}
		}
	}

	System.out.println("Count enterance nodes:"+cntENodes);

	int[] enteranceNodes=new int[cntENodes];
	int j=0;
	for(int i=0;i<N;i++) {
		if(isEnterance[i]) {
			//System.out.println(i);
			enteranceNodes[j++] = i;
		}
	}
	return(enteranceNodes);
}

public int[] getNodeSequence(int[] typeCnts) {
	int totalNodes=IntStream.of(typeCnts).sum();
	int nTypes=typeCnts.length;
	int[] nodeTypes=new int[totalNodes+1];//start from 0
	System.out.println(totalNodes);
	int n = nTypes;
	int[] crntCnts =new int[nTypes];
	for(int i=0;i < nTypes;i++) {//init
		crntCnts[i] = 1;
		nodeTypes[i]=i;
	}

	while(n < totalNodes) {
		for(int y=0; y < nTypes; y++) {
		  if(((double) crntCnts[y]/n) <= ((double) typeCnts[y]/totalNodes) ) {
			  n++;
			  nodeTypes[n]=y;
			  crntCnts[y]++;
		  }
		  if(n >= totalNodes) break;
		}
	}

	//for(int x:crntCnts) System.out.println(x);
	int[] nodeTypesout;
	nodeTypesout=Arrays.copyOf(nodeTypes, totalNodes);
	return(nodeTypesout);
}
//----------------------------------------------------------------

protected int[] weightedSampleWithoutReplacement(int n, int m, int[] wt,Random generator) {
	/* without accumulated weights*/

	int[] Res = new int[m];

	int rand;
	int i;
    int Sum = 0;
    int n1 = n;

    for(i = 1; i <= n; i++) Sum += wt[i];

	int[] wt1=Arrays.copyOf(wt,n+1);

	for (int j = 0; j < m; j++) {
		rand =  generator.nextInt(Sum);

		// find interval
		for (i = 1; i <= n1; i++) {
			rand-=wt1[i];
			if (rand < 0) {//to avoid getting nodes set to zero degree, must be less than
				Res[j] = i;
				Sum -= wt1[i];
				wt1[i] = 0;// to avoid replacement

				break;
			}
		}

	}

	return (Res);
}

/*protected int getInitGraph(int N, double degree, long seed,int[] subj,int[]obj,int[] inDeg,int m,Random generator) {
	int indexToEdgeList = 0;

	if (degree < 1) {
		throw new IllegalArgumentException("Degree must be more than 1.");
	}

	if (degree > (N-1)) {// max links created at any step is N-1
		throw new IllegalArgumentException("Degree can NOT be more than (N-1).");
	}

//	generator = new Random(seed);// seed

	if(m > 1 ) {
		subj[indexToEdgeList] = 1;
		obj[indexToEdgeList] = 2;// first edge
		indexToEdgeList++;
		inDeg[2] = 2;
	}

	// initial part
	if (m > 2) {
		for (int i = 3; i <= m; i++) {
			int[] tmp = weightedSampleWithoutReplacement(i - 1, 2, inDeg,generator);// new links
			boolean randIndex = generator.nextBoolean();
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

	return(indexToEdgeList);
}*/


}

/* NOT Good
generate bi-partite (up to 4 parts(number of types)):1000 hubs, 500 endpoints etc.
endpoints(same part): connected to each other with power law
-type0,type1,type2,type3,type4:
-parts p0,p1,p2,p3 up to p4: each part contains 1 or more types that can connect
-assign parts to types (Hard code type0 as hubs): start with hubs (sort and FIFS) assign remaining
-What is d? d is one parameter to the algorithm.
-Call graphGenerator for each part with number of nodes in that part and d.
*/
