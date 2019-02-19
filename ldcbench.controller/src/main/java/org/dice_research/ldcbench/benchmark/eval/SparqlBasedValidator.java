package org.dice_research.ldcbench.benchmark.eval;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.SimpleCachingTripleCreator;
import org.dice_research.ldcbench.rdf.TripleCreator;

public class SparqlBasedValidator implements GraphValidator {

    protected final QueryExecutionFactory qef;

    protected SparqlBasedValidator(QueryExecutionFactory qef) {
        this.qef = qef;
    }

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

    protected boolean exists(TripleCreator creator, Graph graph, int[] edge) {
        Query q = QueryFactory.create();
        q.setQueryAskType();
        ElementTriplesBlock triples = new ElementTriplesBlock();
        triples.addTriple(creator.createTriple(edge[0], edge[1], edge[2], graph.getExternalNodeId(edge[2]),
                graph.getGraphId(edge[2])));
        q.setQueryPattern(triples);
        try(QueryExecution qe = qef.createQueryExecution(q)) {
            return qe.execAsk();
        }
    }

}
