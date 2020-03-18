package org.dice_research.ldcbench.benchmark.eval.supplier.graph;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class FileBasedGraphSupplier implements GraphSupplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedGraphSupplier.class);

    protected String[] graphFiles;
    protected String[] resourceUriTemplates;
    protected String[] accessUriTemplates;

    public FileBasedGraphSupplier(String[] graphFiles, String[] resourceUriTemplates, String[] accessUriTemplates) {
        this.graphFiles = graphFiles;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
    }

    @Override
    public int getNumberOfGraphs() {
        return graphFiles.length;
    }

    @Override
    public Graph getGraph(int id) {
        try {
            return SerializationHelper.deserialize(FileUtils.readFileToByteArray(new File(graphFiles[id])));
        } catch (Exception e) {
            LOGGER.error("Couldn't load graph #" + id + ". Returning null.", e);
        }
        return null;
    }

    @Override
    public String[] getResourceUriTemplates() {
        return resourceUriTemplates;
    }

    @Override
    public String[] getAccessUriTemplates() {
        return accessUriTemplates;
    }

}
