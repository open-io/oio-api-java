package io.openio.sds.logging;

public class SdsLoggerFactory {

    private static int loaded = -1;

    public static SdsLogger getLogger(Class<?> c) {
        ensureLoaded();
        switch (loaded) {
        case 1:
            return new Log4jLogger(c);
        case 0:
        default:
            return new BaseLogger(c);
        }
    }

    private static void ensureLoaded() {
        if (-1 == loaded) {
            try {
                Class.forName("org.apache.log4j.LogManager");
                loaded = 1;
            } catch (ClassNotFoundException e) {
                loaded = 0;
            }
        }
    }

}
