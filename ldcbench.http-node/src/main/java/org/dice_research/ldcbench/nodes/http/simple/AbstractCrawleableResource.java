package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Predicate;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.springframework.http.MediaType;

public abstract class AbstractCrawleableResource implements CrawleableResource {

    public static final String DEFAULT_CONTENT_TYPE = "*/*";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * A predicate deciding whether a given request is answered with this resources
     * or not.
     */
    protected final Predicate<Request> predicate;
    protected MediaType defaultContentType;

    public AbstractCrawleableResource(Predicate<Request> predicate) {
        this(predicate, DEFAULT_CONTENT_TYPE);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String defaultContentType) {
        this.predicate = predicate;
        setDefaultContentType(DEFAULT_CONTENT_TYPE);
    }

    public boolean handleRequest(Request request, Response response, OutputStream out) throws SimpleHttpException {
        // Check whether the request can be answered by this resource
        if (!predicate.test(request)) {
            return false;
        }
        // Get the response type
        MediaType responseType = getResponseType(request.getValues(ACCEPT_HEADER).iterator());
        if (responseType == null) {
            throw new SimpleHttpException("Couldn't find a fitting content type in the list of accepted types ("
                    + request.getValues(ACCEPT_HEADER).toString() + ").", Status.NOT_ACCEPTABLE);
        }
        // Set the response type
        response.setContentType(responseType.toString());
        return handleRequest(request.getTarget(), responseType, response, out);
    }

    protected MediaType getResponseType(Iterator<String> iterator) {
        return defaultContentType;
    }

    protected abstract boolean handleRequest(String target, MediaType responseType, Response response, OutputStream out) throws SimpleHttpException;

    public void setDefaultContentType(String defaultContentType) throws IllegalArgumentException {
        setDefaultContentType(MediaType.parseMediaType(defaultContentType));
    }

    public void setDefaultContentType(MediaType defaultContentType) {
        this.defaultContentType = defaultContentType;
    }
}
