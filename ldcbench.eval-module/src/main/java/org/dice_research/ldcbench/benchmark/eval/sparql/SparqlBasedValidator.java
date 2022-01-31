package org.dice_research.ldcbench.benchmark.eval.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Call;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_IsLiteral;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.XSD;
import org.dice_research.ldcbench.benchmark.eval.GraphValidator;
import org.dice_research.ldcbench.benchmark.eval.ValidationCounter;
import org.dice_research.ldcbench.benchmark.eval.ValidationResult;
import org.dice_research.ldcbench.benchmark.eval.supplier.graph.GraphSupplier;
import org.dice_research.ldcbench.benchmark.eval.supplier.pattern.TripleBlockStreamSupplier;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.rdf.TripleCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link GraphValidator} interface querying the crawled
 * graph triples from the given SPARQL endpoint.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SparqlBasedValidator implements GraphValidator, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlBasedValidator.class);

    protected static final long SPARQL_QUERY_DELAY = 250;

    /**
     * Query execution factory used for the communication with the SPARQL endpoint.
     */
    protected final QueryExecutionFactory qef;

    /**
     * Constructor.
     *
     * @param qef Query execution factory used for the communication with the SPARQL
     *            endpoint.
     */
    protected SparqlBasedValidator(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    /**
     * Creates a {@link SparqlBasedValidator} instance which uses the given SPARQL
     * endpoint.
     *
     * @param endpoint the URL of the SPARQL endpoint which will be used by the
     *                 created instance.
     * @return the created instance
     */
    public static SparqlBasedValidator create(String endpoint) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
//        qef = new QueryExecutionFactoryDelay(qef, SPARQL_QUERY_DELAY);
        return new SparqlBasedValidator(qef);
    }

    @Override
    public ValidationResult validate(TripleBlockStreamSupplier supplier, int graphId) {
        ValidationCounter counter = new ValidationCounter();
        try {
            supplier.getTripleBlocks(graphId).map(this::results)
                    .forEach(a -> Stream.of(a).forEach(b -> counter.accept(b)));
            ValidationResult result = counter.getValidationResult();
            LOGGER.debug("Validation result from graph {}: {}", graphId, result);
            return result;
        } catch (Exception e) {
            System.out.println("graphId = " + graphId);
            throw e;
        }
    }

    @Deprecated
    public ValidationResult validate(GraphSupplier supplier, int graphId) {
        Graph graph = supplier.getGraph(graphId);
        Objects.requireNonNull(graph, "Got null for graph #" + graphId);
        ValidationCounter counter = new ValidationCounter();
        QueryPatternCreator creator;
        if (supplier.getAccessUriTemplates()[graphId].matches(".*:5000/")) {
            LOGGER.debug("Using CKAN pattern creator to validate results from graph {}", graphId);
            creator = new CkanQueryPatternCreator(graphId, supplier.getResourceUriTemplates(),
                    supplier.getAccessUriTemplates());
        } else {
            LOGGER.debug("Using Simple pattern creator to validate results from graph {}", graphId);
            creator = new SimpleQueryPatternCreator(graphId, supplier.getResourceUriTemplates(),
                    supplier.getAccessUriTemplates());
        }
        try {
            IntStream.range(0, graph.getNumberOfNodes()).parallel().mapToObj(
                    n -> new int[][] { new int[] { n }, graph.outgoingEdgeTypes(n), graph.outgoingEdgeTargets(n) })
                    .flatMap(edgeData -> {
                        int[][] edges = new int[edgeData[1].length][];
                        for (int i = 0; i < edges.length; ++i) {
                            edges[i] = new int[] { edgeData[0][0], edgeData[1][i], edgeData[2][i] };
                        }
                        return Arrays.stream(edges);
                    }).map(e -> results(creator, graph, e)).forEach(a -> Stream.of(a).forEach(b -> counter.accept(b)));
            ValidationResult result = counter.getValidationResult();
            LOGGER.debug("Validation result from graph {}: {}", graphId, result);
            return result;
        } catch (Exception e) {
            System.out.println("graphId = " + graphId);
            System.out.println(Arrays.toString(supplier.getResourceUriTemplates()));
            System.out.println(Arrays.toString(supplier.getAccessUriTemplates()));
            throw e;
        }
    }

    /**
     * Checks whether the given edge exists by creating a {@link Triple} using the
     * given {@link TripleCreator}.
     *
     * @param creator used for creating a {@link Triple} instance of the given edge
     * @param graph   used to handle the special case that the target of the edge is
     *                an external node of the graph
     * @param edge    the edge that is expected to exist in the crawled graph and
     *                that should be checked
     * @return {@code true} if the edge exists, else {@code false}
     */
    protected Boolean[] results(ElementTriplesBlock pattern) {
        // FIXME workaround: evaluate every triple one by one.
        List<Boolean> results = new ArrayList<>();
        Iterator<Triple> iter = pattern.patternElts();
        Triple t;
        boolean queryResult; 
        boolean debug = LOGGER.isDebugEnabled();
        while (iter.hasNext()) {
            t = iter.next();
            // FIXME workaround: do not evaluate triples which have a time literal since
            // Virtuoso seems to have an issue with them. Replace that literal with a
            // variable.
            if(t.getObject().isLiteral() && XSD.time.getURI().equals(t.getObject().getLiteralDatatypeURI())) {
                t = new Triple(t.getSubject(), t.getPredicate(), NodeFactory.createVariable("v"));
            }
            queryResult = execute(t);
            if(!queryResult && debug) {
                LOGGER.debug("Couldn't find triple " + t.toString());
            }
            results.add(queryResult);
        }
        return results.toArray(new Boolean[results.size()]);

//        Query q = QueryFactory.create();
//        q.setQueryAskType();
//        int expected = 0;
//        ElementTriplesBlock whereBlock = new ElementTriplesBlock();
//        Iterator<Triple> iter = pattern.patternElts();
//        Triple t;
//        int vCount = 0;
//        while (iter.hasNext()) {
//            t = iter.next();
//            // FIXME workaround: do not evaluate triples which have a time literal since
//            // Virtuoso seems to have an issue with them. Replace that literal with a
//            // variable.
//            if (t.getObject().isLiteral() && XSD.time.getURI().equals(t.getObject().getLiteralDatatypeURI())) {
//                // replace the literal and ensure that the next literal will get a different
//                // variable
//                t = new Triple(t.getSubject(), t.getPredicate(), NodeFactory.createVariable("v" + vCount));
//                ++vCount;
//            }
//            whereBlock.addTriple(t);
//            expected++;
//        }
//        q.setQueryPattern(pattern);
//        try (QueryExecution qe = qef.createQueryExecution(q)) {
//            boolean result = execAskQuery(qe, 5, 5000);
//            if (!result) {
//                LOGGER.debug("Didn't get expected pattern: {}", pattern.toString().replace("\n", " "));
//            }
//            return IntStream.range(0, expected).mapToObj(i -> result).toArray(Boolean[]::new);
//        } catch (Exception e) {
//            LOGGER.error("Failure when executing query: {}", q.toString().replace("\n", " "));
//            throw e;
//        }
    }

    protected boolean execute(Triple triple) {
        Query q = QueryFactory.create();
        q.setQueryAskType();
        ElementGroup pattern = new ElementGroup();
        Triple queryTriple = triple;
        ElementFilter filterPattern = null;

        if (triple.getObject().isBlank() || triple.getObject().isLiteral()) {
            Var objVar = Var.alloc("obj");
            queryTriple = new Triple(triple.getSubject(), triple.getPredicate(),
                    objVar);
            Expr ObjExpr = new ExprVar(objVar).getExpr();
            Expr filterExpr = null;
            if (triple.getObject().isBlank())
                filterExpr = new E_IsBlank(ObjExpr);
            else if (triple.getObject().isLiteral())
                filterExpr = new E_IsLiteral(ObjExpr);
            filterPattern = new ElementFilter(filterExpr);
        }

        ElementTriplesBlock triplePattern = new ElementTriplesBlock();
        triplePattern.addTriple(queryTriple);
        ExprVar var = new ExprVar("g");
        ElementNamedGraph namedGraphPattern = new ElementNamedGraph(var.getAsNode(), triplePattern);
        pattern.addElement(namedGraphPattern);

        if (filterPattern != null)
            pattern.addElementFilter(filterPattern);

        q.setQueryPattern(pattern);
        try (QueryExecution qe = qef.createQueryExecution(q)) {
            return execAskQuery(qe, 5, 5000);
        } catch (Exception e) {
            LOGGER.error("Failure when executing query: {}", q.toString().replace("\n", " "));
            throw e;
        }
    }

    /**
     * Checks whether the given edge exists by creating a {@link Triple} using the
     * given {@link TripleCreator}.
     *
     * @param creator used for creating a {@link Triple} instance of the given edge
     * @param graph   used to handle the special case that the target of the edge is
     *                an external node of the graph
     * @param edge    the edge that is expected to exist in the crawled graph and
     *                that should be checked
     * @return {@code true} if the edge exists, else {@code false}
     */
    @Deprecated
    protected Boolean[] results(QueryPatternCreator creator, Graph graph, int[] edge) {
        Query q = QueryFactory.create();
        q.setQueryAskType();
        ElementTriplesBlock pattern = creator.create(edge[0], edge[1], edge[2], graph.getExternalNodeId(edge[2]),
                graph.getGraphId(edge[2]),graph.getNodeType(edge[2]));
        assert !pattern.isEmpty();
        int expected = 0;
        for (Iterator<?> i = pattern.patternElts(); i.hasNext(); i.next()) {
            expected++;
        }
        q.setQueryPattern(pattern);
        try (QueryExecution qe = qef.createQueryExecution(q)) {
            boolean result = execAskQuery(qe, 5, 5000);
            if (!result) {
                LOGGER.debug("Didn't get expected pattern: {}", pattern.toString().replace("\n", " "));
            }
            return IntStream.range(0, expected).mapToObj(i -> result).toArray(Boolean[]::new);
        } catch (Exception e) {
            LOGGER.error("Failure when executing query: {}", q.toString().replace("\n", " "));
            throw e;
        }
    }

    private boolean execAskQuery(QueryExecution qe, int tries, long sleepMillis) {
        for (int i = 0; i < tries - 1; i++) {
            try {
                return qe.execAsk();
            } catch (Exception e) {
                LOGGER.error("Failure when executing query (will try again): {}",
                        qe.getQuery().toString().replace("\n", " "));
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ie) {
                }
            }
        }
        return qe.execAsk();
    }

    @Override
    public void close() throws Exception {
        qef.close();
    }

}
