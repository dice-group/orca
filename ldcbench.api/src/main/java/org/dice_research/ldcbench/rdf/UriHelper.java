package org.dice_research.ldcbench.rdf;

public class UriHelper {

    public static final String DATASET_KEY_WORD = "dataset";
    public static final String PROPERTY_NODE_TYPE = "property";
    public static final String RESOURCE_NODE_TYPE = "resource";

    public static String createUri(String domain, int datasetId, String nodeType, int nodeId) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://");
        builder.append(domain);
        builder.append('/');
        builder.append(DATASET_KEY_WORD);
        builder.append('-');
        builder.append(datasetId);
        builder.append('/');
        builder.append(nodeType);
        builder.append('-');
        builder.append(nodeId);
        return builder.toString();
    }
}
