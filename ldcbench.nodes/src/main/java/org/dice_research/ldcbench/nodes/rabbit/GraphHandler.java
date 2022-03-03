package org.dice_research.ldcbench.nodes.rabbit;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphHandler extends DataHandler implements Runnable {

private static final Logger LOGGER = LoggerFactory.getLogger(GraphHandler.class);

    protected List<Graph> graphs = new ArrayList<Graph>();

    public GraphHandler(SimpleFileReceiver receiver) {
        super(receiver);
    }

    @Override
    public void run() {
        try {
            super.run();
            if(!encounteredError()) {
                for (String myFiles : receivedFiles) {
                    handleData(Files.readAllBytes(new File(myFiles).toPath()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while reading graph. Increasing error count.", e);
            ++errorCount;
        }
    }

    private void handleData(byte[] data) {
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

}
