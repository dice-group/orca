package org.dice_research.ldcbench.graph.serialization;

import org.dice_research.ldcbench.graph.Graph;
import java.nio.ByteBuffer;

/**
 * A helper for serializing and deserializing graphs.
 * Uses serializers which implement Serializer interface.
 */
public class SerializationHelper {

    /**
     * Serializes given Graph using given Serializer.
     * Serialization includes the information about which Serializer was used.
     *
     * @param serializerClass a Class which would be used to serialize the graph
     * @param graph a Graph to serialize
     * @return byte array containing the serialization of the graph
     */
    public static byte[] serialize(Class<?> serializerClass, Graph graph) throws InstantiationException, IllegalAccessException {
        if (!Serializer.class.isAssignableFrom(serializerClass)) {
            throw new InstantiationException("Specified serializer class does not implement Serializer interface");
        }

        Serializer serializer = (Serializer) serializerClass.newInstance();
        byte[] data = serializer.serialize(graph);

        byte[] serializerName = serializerClass.getName().getBytes();
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE + serializerName.length + data.length);
        buf.putInt(serializerName.length);
        buf.put(serializerName);

        buf.put(data);
        return buf.array();
    }

    /**
     * Deserializes given byte array into a Graph
     * using a Serializer implementation encoded in the serialized data.
     *
     * @param data byte array containing the serialization of a graph
     * @return deserialized Graph
     */
    public static Graph deserialize(byte[] data) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        int serializerNameLength = buf.getInt();
        byte[] serializerName = new byte[serializerNameLength];
        buf.get(serializerName, 0, serializerNameLength);

        Class<?> serializerClass = Class.forName(new String(serializerName));
        if (!Serializer.class.isAssignableFrom(serializerClass)) {
            throw new InstantiationException("Specified serializer class does not implement Serializer interface");
        }

        Serializer serializer = (Serializer) serializerClass.newInstance();
        return serializer.deserialize(buf.compact().array());
    }

}
