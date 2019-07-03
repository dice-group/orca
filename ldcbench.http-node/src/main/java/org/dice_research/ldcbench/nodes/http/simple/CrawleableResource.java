package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface CrawleableResource {

    public boolean handleRequest(Request request, Response response,
            OutputStream out) throws SimpleHttpException;

}
