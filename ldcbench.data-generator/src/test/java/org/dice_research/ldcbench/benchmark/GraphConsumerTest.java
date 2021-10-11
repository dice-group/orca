package org.dice_research.ldcbench.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import org.dice_research.ldcbench.benchmark.DataGenerator.Types;
import org.dice_research.ldcbench.graph.GraphMetadata;
import org.junit.Assert;
import org.junit.Test;

public class GraphConsumerTest {

    private GraphMetadata receivedGM = null;

    @Test
    public void testGraphMetadataSubmission() throws IOException {
        final Semaphore mutex = new Semaphore(0);
        final int SENDER_ID = 7;
        receivedGM = null;

        GraphConsumer consumer = new GraphConsumer(null) {
            public void handleRdfGraph(int senderId, GraphMetadata gm) {
                try {
                    Assert.assertEquals(SENDER_ID, senderId);
                    receivedGM = gm;
                } finally {
                    mutex.release();
                }
            }
        };

        GraphMetadata originalGM = new GraphMetadata();
        originalGM.numberOfNodes = 100;
        originalGM.entranceNodes = new int[] { 0, 1, 2, 23, 42, 99 };

        ByteBuffer header = ByteBuffer.allocate(2 * (Integer.SIZE / Byte.SIZE));
        header.putInt(SENDER_ID); // generatorId
        header.putInt(Types.RDF_GRAPH_GENERATOR.ordinal());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(header.array(), 0, header.capacity());
        ObjectOutputStream output = new ObjectOutputStream(buf);
        output.writeObject(originalGM);
        byte[] body = buf.toByteArray();

        consumer.handleDelivery(null, null, null, body);
        
        Assert.assertEquals(originalGM, receivedGM);
    }

}
