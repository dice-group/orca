package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.Set;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects when a disallowed path (from a provided set) is requested.
 */
public class DisallowedResource extends AbstractCrawleableResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpServerComponent.class);

    public DisallowedResource(Set<String> paths) {
        super(r -> paths.contains(r.getPath().toString()));
    }

    @Override
    protected boolean handleRequest(String target, MediaType responseType, Response response, OutputStream out)
            throws SimpleHttpException {
        LOGGER.info("Disallowed resource requested: {}", target);
        try {
            response.setStatus(Status.NO_CONTENT);
        } catch (Exception e) {
            new SimpleHttpException("Error while handling request.", e, Status.INTERNAL_SERVER_ERROR);
        }
        return true;
    }
}
