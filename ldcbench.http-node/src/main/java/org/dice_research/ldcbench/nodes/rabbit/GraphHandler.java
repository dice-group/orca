package org.dice_research.ldcbench.nodes.rabbit;

import org.hobbit.core.rabbit.SimpleFileReceiver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphHandler implements Runnable {

private static final Logger LOGGER = LoggerFactory.getLogger(GraphHandler.class);

    protected SimpleFileReceiver receiver;
    protected List<Graph> graphs = new ArrayList<Graph>();
    protected int errorCount = 0;

    public GraphHandler(SimpleFileReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try {
            String receiveOutputDir = getTempDir();
            String receivedFile = receiver.receiveData(receiveOutputDir)[0];
            // FIXME
            handleData(Files.readAllBytes(new File(receiveOutputDir, receivedFile).toPath()));
        } catch (Exception e) {
            LOGGER.error("Error while reading graph. Increasing error count.", e);
            ++errorCount;
        }
    }

    private static String getTempDir() throws IOException {
        File tempFile = File.createTempFile("GraphHandler", "Temp");
        if (!tempFile.delete()) {
            return null;
        }
        if (!tempFile.mkdir()) {
            return null;
        }
        return tempFile.getAbsolutePath();
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

    public int getErrorCount() {
        return errorCount;
    }

    public boolean encounteredError() {
        return errorCount > 0;
    }
}
