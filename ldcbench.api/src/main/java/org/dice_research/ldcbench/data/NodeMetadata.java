package org.dice_research.ldcbench.data;

import java.io.Serializable;

public class NodeMetadata implements Serializable {
    static final long serialVersionUID = -1;

    private String container;
    private String resourceUriTemplate;
    private String accessUriTemplate;

    public void setContainer(String value) {
        container = value;
    }

    public String getContainer() {
        return container;
    }

    public void setResourceUriTemplate(String value) {
        resourceUriTemplate = value;
    }

    public String getResourceUriTemplate() {
        return resourceUriTemplate;
    }

    public void setAccessUriTemplate(String value) {
        accessUriTemplate = value;
    }

    public String getAccessUriTemplate() {
        return accessUriTemplate;
    }

    @Override
    public String toString() {
        return "NodeMetadata {container=" + container + ", resourceUriTemplate=" + resourceUriTemplate + ", accessUriTemplate=" + accessUriTemplate + "}";
    }
}
