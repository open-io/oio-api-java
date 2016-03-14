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
public class SocketPool {

    private static final SdsLogger logger = SdsLoggerFactory
            .getLogger(SocketPool.class);

    private LinkedBlockingQueue<PooledSocket> q;
    private OioHttpSettings settings;
    private AtomicInteger leased;
    private InetSocketAddress target;
    private Thread selfCleaner;

    SocketPool(OioHttpSettings settings, InetSocketAddress target) {
        this(settings, target, false);
    }

    SocketPool(OioHttpSettings settings, InetSocketAddress target,
            boolean startCleaner) {
        this.settings = settings;
        this.leased = new AtomicInteger(0);
        this.target = target;
        this.q = new LinkedBlockingQueue<PooledSocket>();
        if (startCleaner) {
            this.selfCleaner = new SelfCleaner();
            this.selfCleaner.start();
        }
    }

    public PooledSocket lease() {
        PooledSocket sock = q.poll();
        if (null == sock) {
            sock = tryCreate();
            if (null == sock) {
                try {
                    sock = q.poll(settings.pooling().maxConnWait(),
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

    public SocketPool release(PooledSocket sock) {
        q.offer(sock.lastUsage(System.currentTimeMillis()));
        return this;
    }

    public int size() {
        return q.size();
    }

    private PooledSocket tryCreate() {
        if (canCreate())
            return create();
        return null;
    }

    private boolean canCreate() {
        return 0 < settings.pooling().maxConnPerTarget() - leased.get();
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

    void clean() {
        long t = System.currentTimeMillis();
        PooledSocket p = q.peek();
        if (null == p)
            return;
        boolean stop = false;
        if (t - p.lastUsage() > settings.pooling().socketIdleTimeout()) {
            while (null != (p = q.poll()) && !stop) {
                if (t - p.lastUsage() > settings.pooling()
                        .socketIdleTimeout()) {
                    p.quietClose();
                } else {
                    stop = true;
                    q.offer(p);
                }
            }
        }
    }

    private class SelfCleaner extends Thread {

        SelfCleaner() {
            super("cleaner_" + target.toString());
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                sleep(settings.pooling().cleanDelay() * 1000);
                while (true) {
                    sleep(settings.pooling().cleanRate() * 1000);
                    clean();
                }
            } catch (InterruptedException e) {
                logger.debug("Pool cleaner thread interrupted");
            }
        }
    }

}
