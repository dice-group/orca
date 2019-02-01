package org.dice_research.ldcbench.graph.serialization;

import org.dice_research.ldcbench.graph.Graph;

/**
 * A simple interface for graph serializers.
 */
public interface Serializer {

    /**
     * Serializes given Graph
     * using implementation-defined serialization.
     *
     * @param graph a Graph to serialize
     * @return byte array containing the serialization of the graph
     */
    public byte[] serialize(Graph graph);

    /**
     * Deserializes given byte array into a Graph
     * using implementation-defined serialization.
     *
     * @param data byte array containing the serialization of a graph
     * @return deserialized Graph
     */
    public Graph deserialize(byte[] data);

}
