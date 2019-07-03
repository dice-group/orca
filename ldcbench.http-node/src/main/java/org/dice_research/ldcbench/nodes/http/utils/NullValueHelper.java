package org.dice_research.ldcbench.nodes.http.utils;

/**
 * A very simple class for handling {@code null} values by using a default value
 * instead.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class NullValueHelper {

    /**
     * Returns the given default value if the given value is {@code null}, else the
     * given value is returned.
     *
     * @param value
     *            the value that is returned if it is not {@code null}
     * @param defaultValue
     *            the default value returned if the value is {@code null}
     * @return
     */
    public static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
