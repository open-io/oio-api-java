package io.openio.sds.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PooledSocket extends Socket {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(PooledSocket.class);

    private SocketPool pool;
    private AtomicBoolean pooled;
    private long lastUsage;

    PooledSocket(SocketPool pool) {
        super();
        this.pool = pool;
        this.pooled = new AtomicBoolean(false);
    }

    @Override
    public void close() throws IOException {
        if (!pooled.get()) {
            pool.release(this);
            pooled.set(true);
        } 
    }

    void quietClose() {
        try {
            super.close();
        } catch (IOException e) {
            logger.warn("Unable to close socket, possible leak", e);
        }
    }

    PooledSocket markUnpooled() {
        this.pooled.set(false);
        return this;
    }

    long lastUsage() {
        return lastUsage;
    }

    public PooledSocket lastUsage(long t) {
        this.lastUsage = t;
        return this;
    }

}
