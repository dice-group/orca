package org.dice_research.ldcbench.nodes.http.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 * A simple resource which redirects all incoming requests to a given target
 * URL.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RedirectingResource implements CrawleableResource {

    private static final String LOCATION_KEY_WORD = "Location: ";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    protected byte[] redirectionString;
    protected Charset charset;

    /**
     *
     *
     * @param redirectionTarget
     */
    public RedirectingResource(String redirectionTarget) {
        this(redirectionTarget, DEFAULT_CHARSET);
    }

    public RedirectingResource(String redirectionTarget, Charset charset) {
        this.charset = charset;
        redirectionString = (LOCATION_KEY_WORD + redirectionTarget).getBytes(charset);
    }

    @Override
    public boolean handleRequest(Request request, Response response, OutputStream out) throws SimpleHttpException {
        response.setStatus(Status.MOVED_PERMANENTLY);
        try {
            out.write(redirectionString);
        } catch (IOException e) {
            throw new SimpleHttpException("Couldn't write redirect target.", e, Status.INTERNAL_SERVER_ERROR);
        }
        return true;
    }

}
