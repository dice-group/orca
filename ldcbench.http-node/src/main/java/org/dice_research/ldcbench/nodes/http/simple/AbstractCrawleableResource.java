package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public abstract class AbstractCrawleableResource implements CrawleableResource {

    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String DEFAULT_CONTENT_TYPE = "*/*";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ACCEPT_CHARSET_HEADER = "Accept-Charset";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    protected final Predicate<Request> predicate;
    protected Set<String> availableContentTypes = new HashSet<>();
    protected Set<String> availableCharSets = new HashSet<>();
    protected String defaultCharset;
    protected String defaultContentType;

    public AbstractCrawleableResource(Predicate<Request> predicate) {
        this(predicate, new String[0], new String[0]);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String[] contentTypes) {
        this(predicate, new String[0], contentTypes);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String[] charsets, String[] contentTypes) {
        this(predicate, DEFAULT_CHARSET, DEFAULT_CONTENT_TYPE, charsets, contentTypes);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String defaultCharset, String defaultContentType,
            String[] charsets, String[] contentTypes) {
        this.predicate = predicate;
        this.availableContentTypes.addAll(Arrays.asList(contentTypes));
        this.availableCharSets.addAll(Arrays.asList(charsets));
        this.defaultCharset = defaultCharset;
        this.defaultContentType = defaultContentType;
    }

    public boolean handleRequest(Request request, Response response, OutputStream out) throws SimpleHttpException {
        if (!predicate.test(request)) {
            return false;
        }
        String acceptedContentType = null;
        Iterator<String> iter = request.getValues(ACCEPT_HEADER).iterator();
        if (availableContentTypes.size() > 0) {
            // Search for a matching content type
            while ((acceptedContentType == null) && iter.hasNext()) {
                acceptedContentType = iter.next();
                if (!availableContentTypes.contains(acceptedContentType)) {
                    acceptedContentType = null;
                }
            }
        } else {
            // If this crawleable resource has no content type restriction, take the first
            // one of the request
            if (iter.hasNext()) {
                acceptedContentType = iter.next();
            } else {
                acceptedContentType = defaultContentType;
            }
        }
        String acceptedCharset = null;
        iter = request.getValues(ACCEPT_CHARSET_HEADER).iterator();
        if (availableContentTypes.size() > 0) {
            // Search for a matching charset
            while ((acceptedCharset == null) && iter.hasNext()) {
                acceptedCharset = iter.next();
                if (!availableContentTypes.contains(acceptedCharset)) {
                    acceptedCharset = null;
                }
            }
        } else {
            // If this crawleable resource has no charset restriction, take the first
            // one of the request
            if (iter.hasNext()) {
                acceptedCharset = iter.next();
            } else {
                acceptedCharset = defaultCharset;
            }
        }
        if ((acceptedCharset != null) && (acceptedContentType != null)) {
            // FIXME workaround that falls back to RDF XML if content type is not available
            Lang lang = RDFLanguages.contentTypeToLang(acceptedContentType);
            if(lang == null) {
                lang = Lang.RDFXML;
                acceptedContentType = Lang.RDFXML.getHeaderString();
            }
            response.setContentType(acceptedContentType + "; charset=" + acceptedCharset);
            return handleRequest(request.getTarget(), lang, acceptedCharset, out);
        } else {
            if (acceptedContentType == null) {
                throw new SimpleHttpException("Couldn't find a fitting content type in the list of accepted types ("
                        + request.getValues(ACCEPT_HEADER).toString() + ").", Status.NOT_ACCEPTABLE);
            } else {
                throw new SimpleHttpException("Couldn't find a fitting charset in the list of accepted charsets ("
                        + request.getValues(ACCEPT_CHARSET_HEADER).toString() + ").", Status.NOT_ACCEPTABLE);
            }
        }
    }

    public abstract boolean handleRequest(String target, Lang lang, String charset, OutputStream out)
            throws SimpleHttpException;

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }
    
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }
}
