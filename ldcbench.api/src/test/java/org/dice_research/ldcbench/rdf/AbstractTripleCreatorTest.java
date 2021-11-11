package org.dice_research.ldcbench.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTripleCreatorTest {
    
    protected TripleCreator creator;
    protected int edgeData[];
    protected String expectedUris[];
    protected boolean hasBlankNode;

    public AbstractTripleCreatorTest(TripleCreator creator, int edgeData[], String expectedUris[], boolean hasBlankNode) {
        this.creator = creator;
        this.edgeData = edgeData;
        this.expectedUris = expectedUris;
        this.hasBlankNode = hasBlankNode;
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
    }

    protected void checkNode(Node node, String expectedUri) {
        Assert.assertTrue("Expected a URI node.", node.isURI());
        Assert.assertEquals(expectedUri, node.getURI());
    }
}
