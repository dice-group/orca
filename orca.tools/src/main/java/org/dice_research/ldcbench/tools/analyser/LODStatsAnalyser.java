package org.dice_research.ldcbench.tools.analyser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.DCAT;

/**
 * A class which analyses a lodstats dump regarding the
 * <code>:dataset &lt;http://www.w3.org/ns/dcat#downloadURL&gt; "URL" .</code>
 * triples.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
@SuppressWarnings("rawtypes")
public class LODStatsAnalyser implements StreamRDF {

    private static final String DCAT_URI = DCAT.downloadURL.getURI().toString();
    private static final NodeTypes[] TYPES = NodeTypes.values();
    
    private static final byte[] NEWLINE = "\"\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] QUOTE = "\"".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CSV_DELIMITTER = "\",\"".getBytes(StandardCharsets.UTF_8);

    private int counts[];
    private Set[] plds;
    private OutputStream os;

    @Override
    public void start() {
        // init
        counts = new int[TYPES.length + 1];
        plds = new Set[counts.length];
        for (int i = 0; i < plds.length; ++i) {
            plds[i] = new HashSet();
        }
    }

    @Override
    public void triple(Triple triple) {
        // Check the predicate
        Node p = triple.getPredicate();
        if (p.isURI() && DCAT_URI.equals(p.getURI())) {
            // Get the object (i.e., the URL)
            String url = triple.getObject().toString();
            Pattern patterns[];
            for (int i = 0; i < TYPES.length; ++i) {
                patterns = TYPES[i].patterns;
                for (int j = 0; j < patterns.length; ++j) {
                    if (patterns[j].matcher(url).matches()) {
                        count(i, url);
                        if (os != null) {
                            try {
                                os.write(QUOTE);
                                IOUtils.write(url, os, StandardCharsets.UTF_8);
                                os.write(CSV_DELIMITTER);
                                IOUtils.write(TYPES[i].toString(), os, StandardCharsets.UTF_8);
                                os.write(NEWLINE);
                            } catch (Exception e) {
                                System.err.println(e.getMessage());
                            }
                        }
                        return;
                    }
                }
            }
            // Does not fit to any category
            if (os != null) {
                try {
                    os.write(QUOTE);
                    IOUtils.write(url, os, StandardCharsets.UTF_8);
                    os.write(CSV_DELIMITTER);
                    IOUtils.write("OTHER", os, StandardCharsets.UTF_8);
                    os.write(NEWLINE);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            count(TYPES.length, url);
        }
    }

    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    @SuppressWarnings("unchecked")
    private synchronized void count(int typeId, String url) {
        ++counts[typeId];
        try {
            URL u = new URL(url);
            plds[typeId].add(u.getAuthority());
        } catch (MalformedURLException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
    }

    @Override
    public void base(String base) {
        // nothing to do
    }

    @Override
    public void prefix(String prefix, String iri) {
        // nothing to do
    }

    @Override
    public void finish() {
        // nothing to do
    }

    /**
     * @return the counts
     */
    public int[] getCounts() {
        return counts;
    }

    /**
     * @return the plds
     */
    public Set[] getPlds() {
        return plds;
    }

    public static enum NodeTypes {

        DUMP(".*\\.ttl$", ".*\\.n3$", ".*\\.nt$", ".*\\.nq$", ".*\\.xml$", ".*\\.turtle$", ".*\\.owl$", ".*\\.rdf$",
                ".*\\.rdf\\?accessType=DOWNLOAD$"),

        ZIPPED_DUMP(".*\\.gz$", ".*\\.zip$", ".*\\.tgz$", ".*\\.bz2$", ".*\\.7z$", ".*\\.xz$"),

        SPARQL(".*/[sS]parql[/]?.*"),

        HTML(".*\\.html$", ".*\\.htm$"),

//        TXT(".*\\.txt$", ".*\\.csv$")

        ;

        public Pattern[] patterns;

        private NodeTypes(String... patterns) {
            this.patterns = new Pattern[patterns.length];
            for (int i = 0; i < patterns.length; ++i) {
                this.patterns[i] = Pattern.compile(patterns[i]);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Not enough arguments. Usage:\nLODStatsAnalyser <file-to-analyse>");
        }
        LODStatsAnalyser analyser = new LODStatsAnalyser();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream("LODStats-analysis-result.txt"))) {
            analyser.setOutputStream(os);
            System.out.println("Analysing " + args[0] + " ...");
            RDFDataMgr.parse(analyser, args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(" ********** Counts ********** ");
        int counts[] = analyser.getCounts();
        for (int i = 0; i < TYPES.length; ++i) {
            System.out.println(TYPES[i] + ": " + counts[i]);
        }
        System.out.println("OTHERS: " + counts[TYPES.length]);

        System.out.println(" **********  PLDs  ********** ");
        Set[] plds = analyser.getPlds();
        for (int i = 0; i < TYPES.length; ++i) {
            System.out.println(TYPES[i] + ": " + plds[i].size());
        }
        System.out.println("OTHERS: " + plds[TYPES.length].size());
    }

}
