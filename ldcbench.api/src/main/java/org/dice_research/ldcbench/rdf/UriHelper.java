package org.dice_research.ldcbench.rdf;

public class UriHelper {

    public static final String DATASET_KEY_WORD = "dataset";
    public static final String PROPERTY_NODE_TYPE = "property";
    public static final String RESOURCE_NODE_TYPE = "resource";
    public static final String LITERAL = "literal-%s";

    public static String createUri(String uriTemplate, int datasetId, String nodeType, int nodeId) {
        String uri = String.format(uriTemplate, DATASET_KEY_WORD, datasetId, nodeType, nodeId);
        return uri;
    }
}
