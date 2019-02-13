package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Status;

public class GraphBasedResource extends AbstractCrawleableResource {

    protected final int domainId;
    protected final String[] domains;
    protected final Graph[] graphs;

    public GraphBasedResource(int domainId, String[] domains, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes) {
        super(predicate, contentTypes);
        this.domainId = domainId;
        this.domains = domains;
        this.graphs = graphs;
    }

    @Override
    public boolean handleRequest(String target, String contentType, String charset, OutputStream out)
            throws SimpleHTTPException {
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        if (lang == null) {
            throw new SimpleHTTPException(
                    "Couldn't transform content type \"" + contentType + "\" into a known RDF language.",
                    Status.INTERNAL_SERVER_ERROR);
        }
        // parse target
        int ids[] = parseIds(target);

        StreamRDF writerStream = StreamRDFWriter.getWriterStream(out, lang);
        writerStream.start();
        StreamOps.sendTriplesToStream(new TripleIterator(this, ids[0], ids[1]), writerStream);
        writerStream.finish();
        return false;
    }

    private int[] parseIds(String target) throws SimpleHTTPException {
        int start = target.indexOf(UriHelper.DATASET_KEY_WORD);
        if (start < 0) {
            throw new SimpleHTTPException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        start += UriHelper.DATASET_KEY_WORD.length() + 1;
        int end = target.indexOf('/', start);
        if (end < 0) {
            throw new SimpleHTTPException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        return new int[] { parseIdAfterKeyword(target, UriHelper.DATASET_KEY_WORD, true),
                parseIdAfterKeyword(target, UriHelper.RESOURCE_NODE_TYPE, false) };
    }

    private int parseIdAfterKeyword(String target, String keyword, boolean terminatedWithSlash)
            throws SimpleHTTPException {
        int start = target.indexOf(keyword);
        if (start < 0) {
            throw new SimpleHTTPException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        start += keyword.length() + 1;
        int end = terminatedWithSlash ? target.indexOf('/', start) : target.length();
        if (end < 0) {
            throw new SimpleHTTPException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        try {
            return Integer.parseInt(target.substring(start, end));
        } catch (NumberFormatException e) {
            throw new SimpleHTTPException("Couldn't find resource at target \"" + target + "\".", e, Status.NOT_FOUND);
        }
    }

    public class TripleIterator implements Iterator<Triple> {

        protected GraphBasedResource parent;
        protected int datasetId;
        protected int nodeId;
        protected int[] targets;
        protected int[] edgeTypes;
        protected int nextTargetId = 0;
        protected Map<Integer, Node> resourceCache = new HashMap<Integer, Node>();
        protected Map<Integer, Node> propertyCache = new HashMap<Integer, Node>();

        public TripleIterator(GraphBasedResource parent, int datasetId, int nodeId) {
            this.parent = parent;
            this.datasetId = datasetId;
            this.nodeId = nodeId;
            targets = parent.graphs[datasetId].outgoingEdgeTargets(nodeId);
            edgeTypes = parent.graphs[datasetId].outgoingEdgeTypes(nodeId);
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
            return new Triple(createNode(nodeId, false), createNode(propertyId, true), createNode(nodeId, false));
        }

        private Node createNode(int nodeId, boolean isProperty) {
            Map<Integer, Node> cache = isProperty ? propertyCache : resourceCache;
            if (cache.containsKey(nodeId)) {
                return cache.get(nodeId);
            }
            int externalId = parent.graphs[datasetId].getExternalNodeId(nodeId);
            String domain;
            int nodeDatasetId = datasetId;
            if (externalId < 0) {
                domain = parent.domains[domainId];
            } else {
                domain = parent.domains[parent.graphs[datasetId].getGraphId(nodeId)];
                externalId = nodeId;
                // TODO get the datasetId on the other server
                nodeDatasetId = 0;
            }
            Node n;
            if (isProperty) {
                n = ResourceFactory
                        .createProperty(UriHelper.creatUri(domain, nodeDatasetId, UriHelper.PROPERTY_NODE_TYPE, nodeId))
                        .asNode();
            } else {
                n = ResourceFactory
                        .createResource(UriHelper.creatUri(domain, nodeDatasetId, UriHelper.RESOURCE_NODE_TYPE, nodeId))
                        .asNode();
            }
            cache.put(nodeId, n);
            return n;
        }

    }

}
