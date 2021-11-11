package org.dice_research.ldcbench.rdf;

import java.util.List;

import org.apache.jena.graph.Triple;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SimpleCachingTripleCreatorTest extends AbstractTripleCreatorTest {

    @Parameters
    public static List<Object[]> testCases() {
        return SimpleTripleCreatorTest.testCases();
    }

    public SimpleCachingTripleCreatorTest(int baseGraphId, String[] resourceUriTemplates, String[] accessUriTemplates, int edge[],
            String expectedUris[], boolean hasBlankNode) {
        super(new SimpleCachingTripleCreator(baseGraphId, resourceUriTemplates, accessUriTemplates), edge, expectedUris, hasBlankNode);
    }

    @Test
    public void test() {
        Triple t = creator.createTriple(edgeData[0], edgeData[1], edgeData[2], edgeData[3], edgeData[4], hasBlankNode);
        checkNode(t.getSubject(), expectedUris[0]);
        checkNode(t.getPredicate(), expectedUris[1]);
        if(hasBlankNode)
            Assert.assertTrue(t.getObject().isBlank());
        else
            checkNode(t.getObject(), expectedUris[2]);

        // Test the caching mechanisms
        if (expectedUris.length == 3 && expectedUris[0].equals(expectedUris[2])) {
            Assert.assertSame(t.getSubject(), t.getObject());
        }
    }
}
