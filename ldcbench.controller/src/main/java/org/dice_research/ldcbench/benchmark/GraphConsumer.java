package org.dice_research.ldcbench.benchmark;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import org.dice_research.ldcbench.benchmark.DataGenerator.Types;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GraphMetadata;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;

public class GraphConsumer extends DefaultConsumer {

    public GraphConsumer(Channel channel) {
        super(channel);
    }

    public boolean filter(int id, int type) { return true; }

    public void handleNodeGraph(int senderId, Graph g) {}

    public void handleRdfGraph(int senderId, GraphMetadata gm) {}

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
            byte[] body) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(body);
        int senderId = buf.getInt();
        int senderType = buf.getInt();
        buf = buf.compact();
        if (filter(senderId, senderType)) {
            if (senderType == Types.NODE_GRAPH_GENERATOR.ordinal()) {
                Graph g;
                try {
                    g = SerializationHelper.deserialize(buf.array());
                } catch (Exception e) {
                    throw new IOException(e);
                }

                handleNodeGraph(senderId, g);
            } else {
                GraphMetadata gm;
                try {
                    gm = (GraphMetadata) new ObjectInputStream(new ByteArrayInputStream(buf.array())).readObject();
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }

                handleRdfGraph(senderId, gm);
            }
        }
    }
}
