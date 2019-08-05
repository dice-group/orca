package org.dice_research.ldcbench.nodes.http.simple;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.nodes.http.dump.DumpFileBuilder;
import org.dice_research.ldcbench.nodes.http.utils.NullValueHelper;
import org.simpleframework.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpFileResource extends AbstractCrawleableResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileResource.class);

    protected static final Lang DEFAULT_LANG = Lang.TURTLE;

    public static DumpFileResource create(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates,
            Graph[] graphs, Predicate<Request> predicate, String[] contentTypes) {
        DumpFileBuilder builder = new DumpFileBuilder(domainId, resourceUriTemplates, accessUriTemplates, graphs,
                DEFAULT_LANG);
        try {
            File dumpFile = builder.build();
            return new DumpFileResource(domainId, resourceUriTemplates, accessUriTemplates, graphs, predicate,
                    contentTypes, dumpFile);
        } catch (IOException e) {
            LOGGER.error("Couldn't create dump file.", e);
        }
        return null;
    }

    protected final int domainId;
    protected final String[] resourceUriTemplates;
    protected final String[] accessUriTemplates;
    protected final Graph[] graphs;
    protected final File dumpFile;
    protected Lang defaultLang = DEFAULT_LANG;

    protected DumpFileResource(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates, Graph[] graphs,
            Predicate<Request> predicate, String[] contentTypes, File dumpFile) {
        super(predicate, NullValueHelper.valueOrDefault(DEFAULT_LANG.getContentType().getCharset(), DEFAULT_CHARSET),
                DEFAULT_LANG.getContentType().getContentType(), new String[0], contentTypes);
        this.domainId = domainId;
        this.resourceUriTemplates = resourceUriTemplates;
        this.accessUriTemplates = accessUriTemplates;
        this.graphs = graphs;
        this.dumpFile = dumpFile;
    }

    @Override
    public boolean handleRequest(String target, Lang lang, String charset, OutputStream out)
            throws SimpleHttpException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(dumpFile))) {
            IOUtils.copy(in, out);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error while writing dump file to stream.", e);
        }
        return false;
    }

}
