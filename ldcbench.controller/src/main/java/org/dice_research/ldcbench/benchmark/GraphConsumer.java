package org.dice_research.ldcbench.benchmark;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.dice_research.ldcbench.benchmark.DataGenerator.types;
import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.serialization.SerializationHelper;

public class GraphConsumer extends DefaultConsumer {

    public GraphConsumer(Channel channel) {
        super(channel);
    }

    public boolean filter(int id, int type) { return true; }

    public void handleNodeGraph(Graph g) {}

    public void handleRdfGraph(Graph g) {}

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
            byte[] body) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(body);
        int senderId = buf.getInt();
        int senderType = buf.getInt();
        if (filter(senderId, senderType)) {
            Graph g;
            try {
                g = SerializationHelper.deserialize(buf.compact().array());
            } catch (Exception e) {
                // FIXME
                return;
            }
            if (senderType == types.NODE_GRAPH_GENERATOR.ordinal()) {
                handleNodeGraph(g);
            } else {
                handleRdfGraph(g);
            }
        }
    }
}
