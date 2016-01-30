package io.openio.sds.common;

public class Check {

    public static void checkArgument(boolean condition, String msg) {
        if (!condition)
            throw new IllegalArgumentException(msg);
    }

    public static void checkArgument(boolean condition) {
        if (!condition)
            throw new IllegalArgumentException();
    }
}
