package org.dice_research.ldcbench.benchmark.eval;

import org.dice_research.ldcbench.graph.Graph;

public interface GraphSupplier {

    public int getNumberOfGraphs();
    
    public Graph getGraph(int id);
    
    public String[] getDomains();
}
