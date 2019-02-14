package org.dice_research.ldcbench.nodes.http.spring;

import java.io.IOException;

import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class TripleStreamConverter extends AbstractHttpMessageConverter<SimpleTripleStream> {

    @Override
    protected boolean supports(Class<?> clazz) {
        return StreamRDF.class.isAssignableFrom(clazz);
    }

    @Override
    protected SimpleTripleStream readInternal(Class<? extends SimpleTripleStream> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("This mapping has not been implemented.");
    }

    @Override
    protected void writeInternal(SimpleTripleStream iterator, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        StreamRDF writerStream = StreamRDFWriter.getWriterStream(outputMessage.getBody(), iterator.getLang());
        writerStream.start();
        StreamOps.sendTriplesToStream(iterator.getIterator(), writerStream);
        writerStream.finish();
    }

}
