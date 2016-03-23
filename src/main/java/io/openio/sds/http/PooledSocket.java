package io.openio.sds.http;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;
import io.openio.sds.pool.Pool;
import io.openio.sds.pool.Poolable;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PooledSocket extends Socket implements Poolable {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(PooledSocket.class);

    private Pool<PooledSocket> pool;
    private AtomicBoolean pooled;
    private long lastUsage;

    PooledSocket(Pool<PooledSocket> pool) {
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

    @Override
    public boolean reusable() {
        return !this.isInputShutdown();
    }

    @Override
    public void lastUsage(long lastUsage) {
        this.lastUsage = lastUsage;
    }

    @Override
    public void markUnpooled() {
        this.pooled.set(false);
    }

    @Override
    public long lastUsage() {
       return this.lastUsage;
    }

}
