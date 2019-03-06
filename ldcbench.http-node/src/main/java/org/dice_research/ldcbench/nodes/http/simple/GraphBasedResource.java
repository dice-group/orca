package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.graph.GraphFactory;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.utils.NullValueHelper;
import org.dice_research.ldcbench.rdf.SimpleCachingTripleCreator;
import org.dice_research.ldcbench.rdf.TripleCreator;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Status;

public class GraphBasedResource extends AbstractCrawleableResource {

    protected static final Lang DEFAULT_LANG = Lang.TURTLE;

    protected final int domainId;
    protected final String[] domains;
    protected final Graph[] graphs;
    protected boolean failIfContentTypeMismatch = false;
    protected Lang defaultLang = DEFAULT_LANG;

    public GraphBasedResource(int domainId, String[] domains, Graph[] graphs, Predicate<Request> predicate,
            String[] contentTypes) {
        super(predicate, NullValueHelper.valueOrDefault(DEFAULT_LANG.getContentType().getCharset(), DEFAULT_CHARSET),
                DEFAULT_LANG.getContentType().getContentType(), new String[0], contentTypes);
        this.domainId = domainId;
        this.domains = domains;
        this.graphs = graphs;
    }

    @Override
    public boolean handleRequest(String target, Lang lang, String charset, OutputStream out)
            throws SimpleHttpException {
        // Lang lang = RDFLanguages.contentTypeToLang(contentType);
        // if ((lang == null) && (failIfContentTypeMismatch)) {
        // throw new SimpleHttpException(
        // "Couldn't transform content type \"" + contentType + "\" into a known RDF
        // language.",
        // Status.INTERNAL_SERVER_ERROR);
        // } else {
        // lang = defaultLang;
        // }
        // parse target
        
        // TODO add a prefix map
        
        int ids[] = parseIds(target);
        TripleIterator iterator = new TripleIterator(this, ids[0], ids[1]);

        try {
            streamData(iterator, out, lang);
        } catch (RiotException e) {
            if (e.getMessage().contains("No serialization for language")) {
                // Try to serialize it with a model
                createModelAndSend(iterator, out, lang);
            } else {
                throw e;
            }
        }
        return true;
    }

    private void streamData(TripleIterator iterator, OutputStream out, Lang lang) {
        StreamRDF writerStream = StreamRDFWriter.getWriterStream(out, lang);
        writerStream.start();
        StreamOps.sendTriplesToStream(iterator, writerStream);
        writerStream.finish();
    }

    private void createModelAndSend(TripleIterator iterator, OutputStream out, Lang lang) {
        org.apache.jena.graph.Graph g = GraphFactory.createDefaultGraph();
        while (iterator.hasNext()) {
            g.add(iterator.next());
        }
        Model model = ModelFactory.createModelForGraph(g);
        model.write(out, lang.getName());
    }

    private int[] parseIds(String target) throws SimpleHttpException {
        int start = target.indexOf(UriHelper.DATASET_KEY_WORD);
        if (start < 0) {
            throw new SimpleHttpException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        start += UriHelper.DATASET_KEY_WORD.length() + 1;
        int end = target.indexOf('/', start);
        if (end < 0) {
            throw new SimpleHttpException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        return new int[] { parseIdAfterKeyword(target, UriHelper.DATASET_KEY_WORD, true),
                parseIdAfterKeyword(target, UriHelper.RESOURCE_NODE_TYPE, false) };
    }

    private int parseIdAfterKeyword(String target, String keyword, boolean terminatedWithSlash)
            throws SimpleHttpException {
        int start = target.indexOf(keyword);
        if (start < 0) {
            throw new SimpleHttpException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        start += keyword.length() + 1;
        int end = terminatedWithSlash ? target.indexOf('/', start) : target.length();
        if (end < 0) {
            throw new SimpleHttpException("Couldn't find resource at target \"" + target + "\".", Status.NOT_FOUND);
        }
        try {
            return Integer.parseInt(target.substring(start, end));
        } catch (NumberFormatException e) {
            throw new SimpleHttpException("Couldn't find resource at target \"" + target + "\".", e, Status.NOT_FOUND);
        }
    }

    public void setDefaultLang(Lang defaultLang) {
        this.defaultLang = defaultLang;
        setDefaultCharset(NullValueHelper.valueOrDefault(defaultLang.getContentType().getCharset(), DEFAULT_CHARSET));
        setDefaultContentType(defaultLang.getContentType().getContentType());
    }

    public class TripleIterator implements Iterator<Triple> {

        protected GraphBasedResource parent;
        protected int datasetId;
        protected int nodeId;
        protected int[] targets;
        protected int[] edgeTypes;
        protected int nextTargetId = 0;
        protected TripleCreator tripleCreator;

        public TripleIterator(GraphBasedResource parent, int datasetId, int nodeId) {
            this.parent = parent;
            this.datasetId = datasetId;
            this.nodeId = nodeId;
            targets = parent.graphs[datasetId].outgoingEdgeTargets(nodeId);
            edgeTypes = parent.graphs[datasetId].outgoingEdgeTypes(nodeId);
            tripleCreator = new SimpleCachingTripleCreator(datasetId, domains);
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
                    parent.graphs[datasetId].getExternalNodeId(targetId), parent.graphs[datasetId].getGraphId(targetId));
        }
    }
}
