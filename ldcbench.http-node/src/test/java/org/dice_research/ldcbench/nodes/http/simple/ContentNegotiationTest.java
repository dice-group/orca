package org.dice_research.ldcbench.nodes.http.simple;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.simpleframework.http.Response;
import org.springframework.http.MediaType;

import junit.framework.Assert;

@RunWith(Parameterized.class)
public class ContentNegotiationTest extends AbstractNegotiatingResource {

    @Parameters
    public static List<Object[]> testCases() {
        List<Object[]> data = new ArrayList<>();

        // 1 type requested and matches exactly the 1 available type
        data.add(new Object[] { new String[] { "text/plain" }, new String[] { "text/plain" },
                MediaType.parseMediaType("text/plain") });
        // 1 type requested and matches the second available type
        data.add(new Object[] { new String[] { "text/plain", "application/rdf+xml" },
                new String[] { "application/rdf+xml" }, MediaType.parseMediaType("application/rdf+xml") });
        // two types requested where the second matches the available type
        data.add(new Object[] { new String[] { "application/rdf+xml" },
                new String[] { "text/plain", "application/rdf+xml" },
                MediaType.parseMediaType("application/rdf+xml") });
        // 1 type requested but does not match any available type
        data.add(new Object[] { new String[] { "application/rdf+xml" }, new String[] { "text/plain" }, null });
        
        return data;
    }

    protected String[] requestedTypes;
    protected MediaType expectedType;

    public ContentNegotiationTest(String[] contentTypes, String[] requestedTypes, MediaType expectedType) {
        super((r -> true), contentTypes);
        this.requestedTypes = requestedTypes;
        this.expectedType = expectedType;
    }

    @Test
    public void test() {
        MediaType chosenType = getResponseType(Arrays.asList(requestedTypes).iterator());
        Assert.assertEquals(expectedType, chosenType);
    }

    @Override
    protected boolean handleRequest(String target, MediaType responseType, Response response, OutputStream out)
            throws SimpleHttpException {
        // nothing to do
        return false;
    }
}
