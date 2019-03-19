package org.dice_research.ldcbench.data;

import java.io.Serializable;

public class NodeMetadata implements Serializable {
    static final long serialVersionUID = -1;

    private String hostname;

    public void setHostname(String value) {
        hostname = value;
    }

    public String getHostname() {
        return hostname;
    }
}
