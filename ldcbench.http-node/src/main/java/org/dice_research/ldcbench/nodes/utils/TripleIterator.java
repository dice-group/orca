package org.dice_research.ldcbench.nodes.utils;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.SimpleCachingTripleCreator;
import org.dice_research.ldcbench.rdf.TripleCreator;

public class TripleIterator implements Iterator<Triple> {

    /**
     * 
     */
    protected Graph[] graphs;
    protected int datasetId;
    protected int nodeId;
    protected int[] targets;
    protected int[] edgeTypes;
    protected int nextTargetId = 0;
    protected TripleCreator tripleCreator;

    public TripleIterator(Graph[] graphs, int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, int datasetId, int nodeId) {
        this.graphs = graphs;
        this.datasetId = datasetId;
        this.nodeId = nodeId;
        targets = graphs[datasetId].outgoingEdgeTargets(nodeId);
        edgeTypes = graphs[datasetId].outgoingEdgeTypes(nodeId);
        tripleCreator = new SimpleCachingTripleCreator(domainId, resourceUriTemplates, accessUriTemplates);
    }

    @Override
    public boolean hasNext() {
        return nextTargetId < targets.length;
    }

    @Override
    public Triple next() {
        int targetId = nextTargetId++;
        return createTriple(targets[targetId], edgeTypes[targetId]);
    }

    private Triple createTriple(int targetId, int propertyId) {
        return tripleCreator.createTriple(nodeId, propertyId, targetId,
                graphs[datasetId].getExternalNodeId(targetId), graphs[datasetId].getGraphId(targetId),
                targetId >= graphs[datasetId].getBlankNodesIndex(), targetId >= graphs[datasetId].getLiteralsIndex());
    }
}