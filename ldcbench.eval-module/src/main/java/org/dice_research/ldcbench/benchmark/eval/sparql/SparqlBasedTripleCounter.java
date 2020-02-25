package org.dice_research.ldcbench.benchmark.eval.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice_research.ldcbench.benchmark.eval.TripleCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlBasedTripleCounter implements TripleCounter, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlBasedTripleCounter.class);

    protected static final String COUNT_QUERY = "SELECT (SUM(?count) AS ?sum) WHERE {"
            + "SELECT ?g (COUNT(*) AS ?count) WHERE { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g }";
    protected static final String COUNT_VARIABLE_NAME = "sum";

    /**
     * Query execution factory used for the communication with the SPARQL endpoint.
     */
    protected final QueryExecutionFactory qef;

    protected final Query countQuery;

    /**
     * Constructor.
     * 
     * @param qef
     *            Query execution factory used for the communication with the SPARQL
     *            endpoint.
     */
    protected SparqlBasedTripleCounter(QueryExecutionFactory qef, Query countQuery) {
        this.qef = qef;
        this.countQuery = countQuery;
    }

    /**
     * Creates a {@link SparqlBasedTripleCounter} instance which uses the given
     * SPARQL endpoint.
     * 
     * @param endpoint
     *            the URL of the SPARQL endpoint which will be used by the created
     *            instance.
     * @return the created instance
     */
    public static SparqlBasedTripleCounter create(String endpoint) {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
        Query countQuery = QueryFactory.create(COUNT_QUERY);
        return new SparqlBasedTripleCounter(qef, countQuery);
    }

    @Override
    public long countTriples() {
        try (QueryExecution qe = qef.createQueryExecution(countQuery)) {
            ResultSet rs = qe.execSelect();
            if (rs.hasNext()) {
                QuerySolution qs = rs.next();
                return qs.getLiteral(COUNT_VARIABLE_NAME).getLong();
            }
        } catch (Exception e) {
            LOGGER.error("Got an exception while querying triple count. Returning -1.", e);
        }
        return -1;
    }

    @Override
    public void close() throws Exception {
        qef.close();
    }
}
