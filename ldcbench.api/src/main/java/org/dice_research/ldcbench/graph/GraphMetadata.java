package org.dice_research.ldcbench.graph;

import java.io.Serializable;
import java.util.Arrays;

public class GraphMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    public int numberOfNodes;
    public int[] entranceNodes;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(entranceNodes);
        result = prime * result + numberOfNodes;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GraphMetadata other = (GraphMetadata) obj;
        if (!Arrays.equals(entranceNodes, other.entranceNodes))
            return false;
        if (numberOfNodes != other.numberOfNodes)
            return false;
        return true;
    }

}
