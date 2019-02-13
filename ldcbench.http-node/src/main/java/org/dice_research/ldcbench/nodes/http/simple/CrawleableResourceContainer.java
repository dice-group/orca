package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link Container} implementation that can be used to host
 * {@link CrawleableResource} instances.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class CrawleableResourceContainer implements Container {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawleableResourceContainer.class);

    /**
     * Resources hosted by this container.
     */
    private CrawleableResource[] resources;

    /**
     * Constructor.
     * 
     * @param resources
     *            resources that should be hosted by this container.
     */
    public CrawleableResourceContainer(CrawleableResource... resources) {
        this.resources = resources;
    }

    @Override
    public void handle(Request request, Response response) {
        int id = 0;
        try (OutputStream out = response.getOutputStream()) {
            while ((id < resources.length) && (resources[id].handleRequest(request, response, out))) {
                ++id;
            }
            if (id >= resources.length) {
                LOGGER.info("Got a request for an unknown URL: \"" + request.getAddress() + "\".");
                response.setStatus(Status.NOT_FOUND);
            } else {
                response.setStatus(Status.OK);
            }
        } catch (SimpleHTTPException e) {
            if (e.status.code < 500) {
                LOGGER.info("Got an HTTP exception. Returning status code \"" + e.status + "\"", e);
            } else {
                // HTTP 500 should be logged as error
                LOGGER.error("Got an HTTP exception. Returning status code \"" + e.status + "\"", e);
            }
            response.setStatus(e.getStatus());
        } catch (Exception e) {
            LOGGER.error("Got exception while processing request.", e);
        }
    }

}
