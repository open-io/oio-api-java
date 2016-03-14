package io.openio.sds.socket;

import java.io.IOException;
import java.net.Socket;

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
    private boolean pooled = false;
    private long lastUsage;

    PooledSocket(SocketPool pool) {
        super();
        this.pool = pool;
    }

    @Override
    public void close() throws IOException {
        if (!pooled) {
            pool.release(this);
            pooled = true;
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
        this.pooled = false;
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
