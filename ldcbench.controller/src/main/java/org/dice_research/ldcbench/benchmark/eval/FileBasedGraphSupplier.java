package org.dice_research.ldcbench.benchmark.eval;

import org.dice_research.ldcbench.graph.Graph;

public class FileBasedGraphSupplier implements GraphSupplier {
    
    protected String[] graphFiles;
    protected String[] domains;

    @Override
    public int getNumberOfGraphs() {
        return graphFiles.length;
    }

    @Override
    public Graph getGraph(int id) {
        // TODO
        return null;
    }

    @Override
    public String[] getDomains() {
        return domains;
    }
    
}
