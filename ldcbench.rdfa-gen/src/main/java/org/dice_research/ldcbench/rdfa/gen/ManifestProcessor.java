package org.dice_research.ldcbench.rdfa.gen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes the manifest.ttl files of the RDFa test suite and extracts the
 * pairs of HTML and TTL files of all tests that are classified as "required".
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class ManifestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestProcessor.class);

    private static final String SELECT_QUERY_FOR_FILES = "SELECT ?f1 ?f2 WHERE { ?t a <http://rdfa.info/vocabs/rdfa-test#PositiveEvaluationTest> . "
            + "?t <http://www.w3.org/2006/03/test-description#classification> <http://www.w3.org/2006/03/test-description#required> . "
            + "?t <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action> ?f1 ."
            + "?t <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result> ?f2 . }";
    private static final String SELECTED_HTML_FILE = "f1";
    private static final String SELECTED_TTL_FILE = "f2";

    /**
     * Creates a mapping of html file to ttl file.
     * 
     * @param manifestFile
     * @return
     */
    public Map<String, String> loadTests(String manifestFile) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(new File(manifestFile)))) {
            return loadTests(is);
        } catch (IOException e) {
            LOGGER.error("Couldn't load manifest file \"" + manifestFile + "\". Returning null.", e);
        }
        return null;
    }

    public Map<String, String> loadTests(InputStream is) {
        Model model = ModelFactory.createDefaultModel();
        model.read(is, "", "TTL");
        Dataset dataset = DatasetFactory.create(model);
        Query query = QueryFactory.create(SELECT_QUERY_FOR_FILES);
        QueryExecution qe = QueryExecutionFactory.create(query, dataset);
        ResultSet rs = qe.execSelect();
        Map<String, String> tests = new HashMap<String, String>();
        QuerySolution qs;
        while (rs.hasNext()) {
            qs = rs.next();
            tests.put(qs.get(SELECTED_HTML_FILE).asResource().getURI(),
                    qs.get(SELECTED_TTL_FILE).asResource().getURI());
        }
        return tests;
    }
}
