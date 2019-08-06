package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.graph.GraphFactory;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.utils.TripleIterator;
import org.dice_research.ldcbench.rdf.UriHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class GraphBasedResource extends AbstractNegotiatingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphBasedResource.class);
    
    protected final int domainId;
    protected final String[] resourceUriTemplates;
    protected final String[] accessUriTemplates;
    protected final Graph[] graphs;
    protected boolean failIfContentTypeMismatch = false;

    public GraphBasedResource(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            Predicate<Request> predicate, String[] contentTypes) {
        super(predicate, contentTypes);
        this.domainId = domainId;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.graphs = graphs;
    }

    @Override
    protected boolean handleRequest(String target, MediaType responseType, Response response, OutputStream out) throws SimpleHttpException {
        Lang lang = RDFLanguages.contentTypeToLang(responseType.toString());
        if(lang == null) {
        lang = RDFLanguages.contentTypeToLang(responseType.getType());
        }
        if(lang == null) {
            LOGGER.error("Couldn't identify negotiated content type. This shouldn't happen!");
        }
        return handleRequest(target, lang, out);
    }

    public boolean handleRequest(String target, Lang lang, OutputStream out)
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
        TripleIterator iterator = new TripleIterator(graphs, domainId, resourceUriTemplates, accessUriTemplates, ids[0],
                ids[1]);

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

    protected int[] parseIds(String target) throws SimpleHttpException {
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

}
