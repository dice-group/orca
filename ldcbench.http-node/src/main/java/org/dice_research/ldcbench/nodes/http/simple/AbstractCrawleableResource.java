package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Predicate;

import org.apache.http.HttpHeaders;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.springframework.http.MediaType;

public abstract class AbstractCrawleableResource implements CrawleableResource {

    /**
     * If the default content type is not set, this content type is used.
     */
    public static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM_VALUE;

    /**
     * A predicate deciding whether a given request is answered with this resources
     * or not.
     */
    protected final Predicate<Request> predicate;
    /**
     * The default content type which is used for all resources that do not define their own content type.
     */
    protected MediaType defaultContentType;

    public AbstractCrawleableResource(Predicate<Request> predicate) {
        this(predicate, DEFAULT_CONTENT_TYPE);
    }

    public AbstractCrawleableResource(Predicate<Request> predicate, String defaultContentType) {
        this.predicate = predicate;
        setDefaultContentType(defaultContentType);
    }

    public boolean handleRequest(Request request, Response response, OutputStream out) throws SimpleHttpException {
        // Check whether the request can be answered by this resource
        if (!predicate.test(request)) {
            return false;
        }
        // Get the response type
        MediaType responseType = getResponseType(request.getValues(HttpHeaders.ACCEPT).iterator());
        if (responseType == null) {
            throw new SimpleHttpException("Couldn't find a fitting content type in the list of accepted types ("
                    + request.getValues(HttpHeaders.ACCEPT).toString() + ").", Status.NOT_ACCEPTABLE);
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
