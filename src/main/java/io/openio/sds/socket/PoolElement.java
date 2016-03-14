package io.openio.sds.socket;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PoolElement {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(PoolElement.class);

    private LinkedBlockingQueue<PooledSocket> queue;
    private OioHttpSettings settings;
    private AtomicInteger leased;
    private InetSocketAddress target;

    PoolElement(OioHttpSettings settings, InetSocketAddress target) {
        this.leased = new AtomicInteger(0);
        this.target = target;
        this.queue = new LinkedBlockingQueue<PooledSocket>();
    }

    boolean canCreate() {
        return 0 < settings.pooling().maxConnPerTarget() - leased.get();
    }

    public PooledSocket lease() {
        PooledSocket sock = queue.poll();
        if (null == sock) {
            sock = tryCreate();
            if (null == sock) {
                try {
                    sock = queue.poll(settings.pooling().maxConnWait(),
                            TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.debug("connection wait interrrupted");
                }
                if (null == sock)
                    throw new OioException(
                            String.format("Unable to get connection to %s",
                                    target.toString()));
            }
        }
        return sock;
    }

    public PoolElement release(PooledSocket sock) {
        queue.offer(sock.lastUsage(System.currentTimeMillis()));
        return this;
    }

    private PooledSocket tryCreate() {
        if (canCreate())
            return create();
        return null;
    }

    private PooledSocket create() {
        try {
            PooledSocket sock = new PooledSocket(this);
            sock.setSendBufferSize(settings.sendBufferSize());
            sock.setReuseAddress(true);
            sock.setReceiveBufferSize(settings.receiveBufferSize());
            sock.connect(target, settings.connectTimeout());
            return sock;
        } catch (IOException e) {
            throw new OioException(format(
                    "Unable to get connection to %s", target.toString()), e);
        }
    }

}
