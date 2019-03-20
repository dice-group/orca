package org.dice_research.ldcbench.nodes.rabbit;

import java.util.ArrayList;
import java.util.List;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.hobbit.core.rabbit.DataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphHandler implements DataHandler {

private static final Logger LOGGER = LoggerFactory.getLogger(GraphHandler.class);

    protected List<Graph> graphs = new ArrayList<Graph>();
    protected int errorCount = 0;

    @Override
    public void handleData(byte[] data) {
        try {
            graphs.add(SerializationHelper.deserialize(data));
        } catch (Exception e) {
            LOGGER.error("Error while deserializing graph. Increasing error count.", e);
            ++errorCount;
        }
    }
    
    public List<Graph> getGraphs() {
        return graphs;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public boolean encounteredError() {
        return errorCount > 0;
    }
}