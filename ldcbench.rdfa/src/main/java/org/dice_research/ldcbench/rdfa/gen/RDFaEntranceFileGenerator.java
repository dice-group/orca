package org.dice_research.ldcbench.rdfa.gen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Generates a simple HTML file that has the given list of outgoing links
 * embedded.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFaEntranceFileGenerator {

    public void generate(File htmlFile, String resourceURI, Set<String> outgoingLinks) throws IOException {
        try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(htmlFile)),
                StandardCharsets.UTF_8)) {
            // Start with the HTML header
            writer.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Some nice title</title>\n</head>\n<body>\n<p resource=\"");
            writer.append(resourceURI);
            writer.append("\">\n");
            // Write the outgoing links.
            for (String link : outgoingLinks) {
                writer.append("<a property=\"http://www.w3.org/2000/01/rdf-schema#seeAlso\" href=\"");
                writer.append(link);
                writer.append("\">");
                writer.append(link);
                writer.append("</a>\n");
            }
            // Write end of HTML page
            writer.append("</p>\n</body>\n</html>\n");
        }

    }

}
