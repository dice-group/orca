package org.dice_research.ldcbench.nodes.rabbit;

import java.io.File;
import java.io.IOException;

import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHandler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataHandler.class);

    protected SimpleFileReceiver receiver;
    protected int errorCount = 0;
    protected String receiveOutputDir;
    protected String receivedFiles[];

    public DataHandler(SimpleFileReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try {
            receiveOutputDir = getTempDir();
            receivedFiles = receiver.receiveData(receiveOutputDir);
            if (!receiveOutputDir.endsWith(File.separator)) {
                receiveOutputDir += File.separator;
            }
            for (int i = 0; i < receivedFiles.length; ++i) {
                receivedFiles[i] = receiveOutputDir + receivedFiles[i];
            }
        } catch (Exception e) {
            LOGGER.error("Error while reading graph. Increasing error count.", e);
            ++errorCount;
        }
    }

    private static String getTempDir() throws IOException {
        File tempFile = File.createTempFile("DataHandler", "temp");
        if (!tempFile.delete()) {
            return null;
        }
        if (!tempFile.mkdir()) {
            return null;
        }
        return tempFile.getAbsolutePath();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public boolean encounteredError() {
        return errorCount > 0;
    }

    public String[] getReceivedFiles() {
        return receivedFiles;
    }
}
