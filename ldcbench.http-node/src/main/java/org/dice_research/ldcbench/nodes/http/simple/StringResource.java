package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.function.Predicate;

import org.simpleframework.http.Request;
import org.simpleframework.http.Status;

/**
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class StringResource extends AbstractCrawleableResource implements CrawleableResource {

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    protected final String content;

    public StringResource(Predicate<Request> predicate, String content) {
        this(predicate, content, DEFAULT_CONTENT_TYPE);
    }

    public StringResource(Predicate<Request> predicate, String content, String contentType) {
        super(predicate, new String[] {contentType});
        this.content = content;
    }

    @Override
    public boolean handleRequest(String target, String contentType, String charsetName, OutputStream out)
            throws SimpleHTTPException {
        try {
            out.write(content.getBytes(charsetName));
        } catch (Exception e) {
            new SimpleHTTPException("Error while writing content.", e, Status.INTERNAL_SERVER_ERROR);
        }
        return true;
    }

}
