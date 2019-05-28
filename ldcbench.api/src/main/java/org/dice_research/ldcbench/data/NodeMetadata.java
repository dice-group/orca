package org.dice_research.ldcbench.data;

import java.io.Serializable;

public class NodeMetadata implements Serializable {
    static final long serialVersionUID = -1;

    private String container;
    private String uriTemplate;

    public void setContainer(String value) {
        container = value;
    }

    public String getContainer() {
        return container;
    }

    public void setUriTemplate(String value) {
        uriTemplate = value;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }
}
