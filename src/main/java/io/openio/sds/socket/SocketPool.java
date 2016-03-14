package io.openio.sds.socket;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPool {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(SocketPool.class);

    private static final String KEY_FORMAT = "%s:%d";
    private static final String CLEANER_NAME = "pool_cleaner";

    private HashMap<String, PoolElement> pool;
    private OioHttpSettings settings;
    private Thread cleaner;

    public SocketPool(OioHttpSettings settings) {
        this.settings = settings;
        if (settings.pooling().enabled()) {
            pool = new HashMap<String, ConcurrentLinkedQueue<PooledSocket>>();
            this.cleaner = new PoolCleaner();
            this.cleaner.start();
        }
    }
    
    public void shutdown() {
        this.cleaner.interrupt();
    }

    public PooledSocket lease(String host, int port) {
        PooledSocket sock = getQueue(host, port).poll();
        return null == sock ? create(host, port) : sock.markUnpooled();
    }
    
    public int size(String host, int port) {
        ConcurrentLinkedQueue<PooledSocket> q = pool.get(key(host, port));
        return null == q ? 0 : q.size();
    }

  
    public void release(PooledSocket sock) {
        pool.get(sock.key()).offer(sock.lastUsage(System.currentTimeMillis()));
    }

    private ConcurrentLinkedQueue<PooledSocket> getQueue(String host,
            int port) {
        String key = key(host, port);
        ConcurrentLinkedQueue<PooledSocket> q = pool.get(key);
        if (null == q) {
            q = new ConcurrentLinkedQueue<PooledSocket>();
            pool.put(key, q);
        }
        return q;
    }

    private String key(String host, int port) {
        return format(KEY_FORMAT, host, port);
    }

  
}
