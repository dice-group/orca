package org.dice_research.ldcbench.benchmark.eval;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.SimpleCachingTripleCreator;
import org.dice_research.ldcbench.rdf.TripleCreator;

/**
 * Implementation of the {@link GraphValidator} interface querying the crawled
 * graph triples from the given SPARQL endpoint.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SparqlBasedValidator implements GraphValidator, AutoCloseable {

    /**
     * Query execution factory used for the communication with the SPARQL endpoint.
     */
    protected final QueryExecutionFactory qef;

    /**
     * Constructor.
     * 
     * @param qef
     *            Query execution factory used for the communication with the SPARQL
     *            endpoint.
     */
    protected SparqlBasedValidator(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    /**
     * Creates a {@link SparqlBasedValidator} instance which uses the given SPARQL
     * endpoint.
     * 
     * @param endpoint
     *            the URL of the SPARQL endpoint which will be used by the created
     *            instance.
     * @return the created instance
     */
    public static SparqlBasedValidator create(String endpoint) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
        return new SparqlBasedValidator(qef);
    }

    @Override
    public ValidationResult validate(GraphSupplier supplier, int graphId) {
        Graph graph = supplier.getGraph(graphId);
        Objects.requireNonNull(graph, "Got null for graph #" + graphId);
        ValidationCounter counter = new ValidationCounter();
        TripleCreator creator = new SimpleCachingTripleCreator(graphId, supplier.getDomains());
        IntStream.range(0, graph.getNumberOfNodes()).parallel()
                .mapToObj(
                        n -> new int[][] { new int[] { n }, graph.outgoingEdgeTypes(n), graph.outgoingEdgeTargets(n) })
                .flatMap(edgeData -> {
                    int[][] edges = new int[edgeData[1].length][];
                    for (int i = 0; i < edges.length; ++i) {
                        edges[i] = new int[] { edgeData[0][0], edgeData[1][i], edgeData[2][i] };
                    }
                    return Arrays.stream(edges);
                }).map(e -> exists(creator, graph, e)).forEach(b -> counter.accept(b));
        return counter.getValidationResult();
    }

    /**
     * Checks whether the given edge exists by creating a {@link Triple} using the
     * given {@link TripleCreator}.
     * 
     * @param creator
     *            used for creating a {@link Triple} instance of the given edge
     * @param graph
     *            used to handle the special case that the target of the edge is an
     *            external node of the graph
     * @param edge
     *            the edge that is expected to exist in the crawled graph and that
     *            should be checked
     * @return {@code true} if the edge exists, else {@code false}
     */
    protected boolean exists(TripleCreator creator, Graph graph, int[] edge) {
        Query q = QueryFactory.create();
        q.setQueryAskType();
        ElementTriplesBlock triples = new ElementTriplesBlock();
        triples.addTriple(creator.createTriple(edge[0], edge[1], edge[2], graph.getExternalNodeId(edge[2]),
                graph.getGraphId(edge[2])));
        q.setQueryPattern(triples);
        try (QueryExecution qe = qef.createQueryExecution(q)) {
            return qe.execAsk();
        }
    }

    @Override
    public void close() throws Exception {
        qef.close();
    }

}
