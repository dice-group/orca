package org.dice_research.ldcbench.microformat.gen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

/**
 * Generates two files - a simple HTML file that has the given list of outgoing
 * links embedded and a ttl file comprising the generated triples.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 * @author Thoren Grüttemeier (thoreng@uni-paderborn.de, Microformat modifications)
 *
 */
public class MicroformatEntranceFileGenerator {

    /**
     * Generates two files - a simple HTML file that has the given list of outgoing
     * links embedded and a ttl file comprising the generated triples.
     *
     * @param htmlFile      the file that will be used to write the HTML file
     * @param ttlFile       the file that will be used to write the ttl file
     * @param resourceURI   the URL of the entrance file
     * @param outgoingLinks the set of outgoing links. Note that the links have to
     *                      be absolute URLs even if they are hosted on the same
     *                      machine.
     * @throws IOException if one of the files can not be written
     */
    public void generate(File htmlFile, File ttlFile, String resourceURI, Set<String> outgoingLinks)
            throws IOException {
        Model model = ModelFactory.createDefaultModel();
        Resource entrancePageResource = model.createResource(resourceURI);
        try (Writer htmlWriter = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(htmlFile)),
                StandardCharsets.UTF_8);
                OutputStream ttlOut = new BufferedOutputStream(new FileOutputStream(ttlFile))) {
            // Start with the HTML header
            htmlWriter.append(
                    "<!DOCTYPE html>\n<html>\n<head>\n<title>Some nice title</title>\n</head>\n<body>\n<a class=\"h-entry\" href=\"");
            htmlWriter.append(resourceURI);
            htmlWriter.append("\">\n");
            // Write the outgoing links.
            for (String link : outgoingLinks) {
                htmlWriter.append("<a class=\"u-url\"="); // use the u-url property
                htmlWriter.append(link);
                htmlWriter.append("\">");
                htmlWriter.append(link);
                htmlWriter.append("</a>\n");
                model.add(entrancePageResource, RDFS.seeAlso, model.createResource(link));
            }
            // Write end of HTML page
            htmlWriter.append("</div>\n</body>\n</html>\n");

            model.write(ttlOut, "TURTLE");
        }
    }

}
