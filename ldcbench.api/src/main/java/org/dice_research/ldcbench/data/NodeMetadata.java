package org.dice_research.ldcbench.data;

import java.io.Serializable;

public class NodeMetadata implements Serializable {
    static final long serialVersionUID = -1;

    private String container;
    private String resourceUriTemplate;
    private String accessUriTemplate;
    private boolean terminated = false;

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

    /**
     * @return the terminated
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @param terminated the terminated to set
     */
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    @Override
    public String toString() {
        return "NodeMetadata {container=" + container + ", resourceUriTemplate=" + resourceUriTemplate + ", accessUriTemplate=" + accessUriTemplate + ", terminated=" + terminated + "}";
    }
}
