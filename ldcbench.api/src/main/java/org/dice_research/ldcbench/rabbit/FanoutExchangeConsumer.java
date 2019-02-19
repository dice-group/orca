package org.dice_research.ldcbench.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.Closeable;
import java.io.IOException;
import org.hobbit.core.rabbit.RabbitQueueFactory;

/**
 * This class implements consuming of byte array data from messages coming through fanout exchange.
 */
public class FanoutExchangeConsumer implements Closeable {
    private Channel channel;

    /**
     * Initializes consuming of messages coming through specified exchange.
     *
     * @param queueFactory
     *            factory to create queues with
     * @param exchangeName
     *            name of the exchange
     */
    public FanoutExchangeConsumer(RabbitQueueFactory queueFactory, String exchangeName) throws IOException {
        channel = queueFactory.getConnection().createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare(exchangeName, "fanout", false, true, null);
        channel.queueBind(queueName, exchangeName, "");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                handle(body);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    @Override
    public void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Abstract method which is called when a message is received.
     *
     * @param body
     *            message body as a byte array
     */
    public void handle(byte[] body) {};
}
