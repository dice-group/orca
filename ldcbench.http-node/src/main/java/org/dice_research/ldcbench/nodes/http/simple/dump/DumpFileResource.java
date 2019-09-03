package org.dice_research.ldcbench.nodes.http.simple.dump;

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
import org.dice_research.ldcbench.nodes.http.simple.AbstractCrawleableResource;
import org.dice_research.ldcbench.nodes.http.simple.SimpleHttpException;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class DumpFileResource extends AbstractCrawleableResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileResource.class);

    public static DumpFileResource create(int domainId, String[] resourceUriTemplates, String[] accessUriTemplates,
            Graph[] graphs, Predicate<Request> predicate, Lang lang, boolean useCompression,int seed) {
        DumpFileBuilder builder = new DumpFileBuilder(domainId, resourceUriTemplates, accessUriTemplates, graphs,
                lang, useCompression,seed);
        try {
            File dumpFile = builder.build();
            return new DumpFileResource(predicate, builder.buildContentType(), dumpFile);
        } catch (IOException e) {
            LOGGER.error("Couldn't create dump file.", e);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Couldn't create dump file.", e);
        } catch (SecurityException e) {
            LOGGER.error("Couldn't create dump file.", e);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Couldn't create dump file.", e);

        }
        return null;
    }

    protected final File dumpFile;

    protected DumpFileResource(Predicate<Request> predicate, String contentType, File dumpFile) {
        super(predicate, contentType);
        this.dumpFile = dumpFile;
    }

    @Override
    protected boolean handleRequest(String target, MediaType responseType, Response response, OutputStream out)
            throws SimpleHttpException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(dumpFile))) {
            IOUtils.copy(in, out);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error while writing dump file to stream.", e);
            throw new SimpleHttpException("Error while writing dump file to stream.", e, Status.INTERNAL_SERVER_ERROR);
        }
    }

}
