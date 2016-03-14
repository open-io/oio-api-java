package io.openio.sds.socket;

import static java.lang.String.format;

import java.net.InetSocketAddress;
import java.util.HashMap;

import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPoolGroup {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(SocketPoolGroup.class);

    private static final String KEY_FORMAT = "%s:%d";
    private static final String CLEANER_NAME = "pool_cleaner";

    private HashMap<String, SocketPool> pool;
    private OioHttpSettings settings;
    private Thread cleaner;

    public SocketPoolGroup(OioHttpSettings settings) {
        this.settings = settings;
        if (settings.pooling().enabled()) {
            pool = new HashMap<String, SocketPool>();
            this.cleaner = new PoolCleaner();
            this.cleaner.start();
        }
    }

    public void shutdown() {
        this.cleaner.interrupt();
    }

    public PooledSocket lease(String host, int port) {
        return getPoolElement(host, port).lease();
    }

    public int size(String host, int port) {
        SocketPool p = pool.get(key(host, port));
        return null == p ? 0 : p.size();
    }

    private SocketPool getPoolElement(String host,
            int port) {
        String key = key(host, port);
        SocketPool p = pool.get(key);
        if (null == p) {
            p = new SocketPool(settings, new InetSocketAddress(host, port));
            pool.put(key, p);
        }
        return p;
    }

    private String key(String host, int port) {
        return format(KEY_FORMAT, host, port);
    }

    private class PoolCleaner extends Thread {

        PoolCleaner() {
            super(CLEANER_NAME);
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                sleep(settings.pooling().cleanDelay() * 1000);
                while (true) {
                    sleep(settings.pooling().cleanRate() * 1000);
                    runOneIteration();
                }
            } catch (InterruptedException e) {
                logger.debug("Pool cleaner thread interrupted");
            }
        }

        private void runOneIteration() {
            for (SocketPool p : pool.values()) {
                p.clean();
            }
        }
    }
}
