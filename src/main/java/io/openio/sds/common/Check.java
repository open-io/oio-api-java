package io.openio.sds.common;

/**
 * Utility class which provides common parameters verification
 *
 */
public class Check {

	/**
	 * Checks the specified condition and throw an {@code IllegalArgumentException} if {@code false}
	 * @param condition the condition to check
	 * @param msg the message to set in the exception
	 * @throws IllegalArgumentException if the specified condition is {@code false}
	 */
    public static void checkArgument(boolean condition, String msg) {
        if (!condition)
            throw new IllegalArgumentException(msg);
    }
    
    /**
	 * Checks the specified condition and throw an {@code IllegalArgumentException} if {@code false}
	 * @param condition the condition to check
	 * @throws IllegalArgumentException if the specified condition is {@code false}
	 */
    public static void checkArgument(boolean condition) {
        if (!condition)
            throw new IllegalArgumentException();
    }
}
