package org.dice_research.ldcbench.benchmark.eval.supplier.bytes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.ldcbench.utils.tar.TarGZBasedTTLModelIterator;
import org.hobbit.utils.test.ModelComparisonHelper;
import org.junit.Assert;
import org.junit.Test;

public class TarGZBasedByteArraySupplierTest {

    @Test
    public void test() throws IOException {
        // Create dataset and model files
        Dataset dataset = DatasetFactory.create();

        Model model1 = ModelFactory.createDefaultModel();
        model1.add(model1.getResource("http://example.org/Class1"), RDF.type, RDFS.Class);
        model1.add(model1.getResource("http://example.org/Property"), RDF.type, RDF.Property);
        dataset.addNamedModel("file:///test1.ttl", model1);
        File test1File = File.createTempFile("test1", ".ttl");
        test1File.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(test1File))) {
            model1.write(os, "TTL");
        }

        Model model2 = ModelFactory.createDefaultModel();
        model2.add(model2.getResource("http://example.org/Class2"), RDF.type, RDFS.Class);
        model2.add(model2.getResource("http://example.org/Class2"), RDFS.label, "Class 2");
        model2.add(model2.getResource("http://example.org/Class2"), RDFS.comment, "This is a description for Class 2.");
        dataset.addNamedModel("file:///test2.ttl", model2);
        File test2File = File.createTempFile("test2", ".ttl");
        test2File.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(test2File))) {
            model2.write(os, "TTL");
        }

        Model model3 = ModelFactory.createDefaultModel();
        model3.add(model3.getResource("http://example.org/Stmt3"), RDF.type, RDF.Statement);
        model3.add(model3.getResource("http://example.org/Stmt3"), RDF.subject,
                model3.getResource("http://example.org/Subject3"));
        model3.add(model3.getResource("http://example.org/Stmt3"), RDF.predicate,
                model3.getResource("http://example.org/prop3"));
        model3.addLiteral(model3.getResource("http://example.org/Stmt3"), RDF.object, 3.0);
        dataset.addNamedModel("file:///test3.ttl", model3);
        File test3File = File.createTempFile("test3", ".ttl");
        test3File.deleteOnExit();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(test3File))) {
            model3.write(os, "TTL");
        }

        // Write files to a tar archive
        File file = File.createTempFile("SupplierTest", ".tar.gz");
        file.deleteOnExit();
        TarArchiveOutputStream outStream = new TarArchiveOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
        outStream.putArchiveEntry(outStream.createArchiveEntry(test1File, "test1.ttl"));
        try (InputStream input = new BufferedInputStream(new FileInputStream(test1File))) {
            IOUtils.copy(input, outStream);
        }
        outStream.closeArchiveEntry();
        outStream.putArchiveEntry(outStream.createArchiveEntry(test2File, "test2.ttl"));
        try (InputStream input = new BufferedInputStream(new FileInputStream(test2File))) {
            IOUtils.copy(input, outStream);
        }
        outStream.closeArchiveEntry();
        outStream.putArchiveEntry(outStream.createArchiveEntry(test3File, "test3.ttl"));
        try (InputStream input = new BufferedInputStream(new FileInputStream(test3File))) {
            IOUtils.copy(input, outStream);
        }
        outStream.closeArchiveEntry();
        outStream.close();

        // Read models from the archive and compare it to the original dataset
        try (TarGZBasedTTLModelIterator iterator = TarGZBasedTTLModelIterator.create(file)) {
            iterator.forEachRemaining(m -> findAndRemove(m, dataset));
        }
        
        // make sure that all models have been removed
        Assert.assertTrue("The dataset is not empty although this was expected.", dataset.isEmpty());
    }

    private void findAndRemove(Model m, Dataset dataset) {
        Iterator<String> graphIter = dataset.listNames();
        String graphUri;
        Model m2;
        if (m == null) {
            System.out.println("NULL");
        }
        boolean modelFound = false;
        while (graphIter.hasNext()) {
            graphUri = graphIter.next();
            m2 = dataset.getNamedModel(graphUri);
            if ((m2 != null) && ModelComparisonHelper.getMissingStatements(m, m2).isEmpty()
                    && ModelComparisonHelper.getMissingStatements(m2, m).isEmpty()) {
                dataset.removeNamedModel(graphUri);
                modelFound = true;
            }
        }
        Assert.assertTrue("Couldn't find a model that matches the read model.", modelFound);
    }
}
