package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public abstract class AbstractCrawleableResource implements CrawleableResource {

    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ACCEPT_CHARSET_HEADER = "Accept-Charset";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    protected final Predicate<Request> predicate;
    protected Set<String> availableContentTypes = new HashSet<>();
    protected Set<String> availableCharSets = new HashSet<>();

    public AbstractCrawleableResource(Predicate<Request> predicate, String... contentTypes) {
        this(predicate, new String[] { DEFAULT_CHARSET }, contentTypes);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String[] charsets, String... contentTypes) {
        this.predicate = predicate;
        this.availableContentTypes.addAll(Arrays.asList(contentTypes));
        this.availableCharSets.addAll(Arrays.asList(charsets));
    }

    public boolean handleRequest(Request request, Response response, OutputStream out) throws SimpleHTTPException {
        if (!predicate.test(request)) {
            return false;
        }
        String acceptedContentType = null;
        Iterator<String> iter = request.getValues(ACCEPT_HEADER).iterator();
        while ((acceptedContentType == null) && iter.hasNext()) {
            acceptedContentType = iter.next();
            if (!availableContentTypes.contains(acceptedContentType)) {
                acceptedContentType = null;
            }
        }
        String acceptedCharset = null;
        iter = request.getValues(ACCEPT_CHARSET_HEADER).iterator();
        while ((acceptedCharset == null) && iter.hasNext()) {
            acceptedCharset = iter.next();
            if (!availableContentTypes.contains(acceptedCharset)) {
                acceptedCharset = null;
            }
        }
        if ((acceptedCharset != null) && (acceptedContentType != null)) {
            response.setContentType(acceptedContentType + "; charset=" + acceptedCharset);
            return handleRequest(request.getTarget(), acceptedContentType, acceptedCharset, out);
        } else {
            if (acceptedCharset == null) {
                throw new SimpleHTTPException("Couldn't find a fitting content type in the list of accepted types ("
                        + request.getValues(ACCEPT_HEADER).toString() + ").", Status.NOT_ACCEPTABLE);
            } else {
                throw new SimpleHTTPException("Couldn't find a fitting charset in the list of accepted charsets ("
                        + request.getValues(ACCEPT_CHARSET_HEADER).toString() + ").", Status.NOT_ACCEPTABLE);
            }
        }
    }

    public abstract boolean handleRequest(String target, String contentType, String charset, OutputStream out)
            throws SimpleHTTPException;

}
