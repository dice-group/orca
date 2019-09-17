package org.dice_research.ldcbench.nodes.components;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

interface AbstractNodeComponent {
    /**
     * Initialization before any data is generated should be performed there.
     * If needed, resourceUriTemplate and accessUriTemplate should be set there,
     * possibly after starting relevant containers.
     */
    public void initBeforeDataGeneration() throws Exception;

    /**
     * Initialization with data generation already done.
     * nodeMetadata[] (for all nodes) and graphs are available at this point.
     */
    public void initAfterDataGeneration() throws Exception;

    default public void addResults(Model model, Resource root) {
    };
}
