package io.openio.sds.socket;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
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

    private ArrayBlockingQueue<PooledSocket> q;
    private OioHttpSettings settings;
    private AtomicInteger leased;
    private InetSocketAddress target;
    private Thread selfCleaner;

    public SocketPool(OioHttpSettings settings, InetSocketAddress target) {
        this(settings, target, false);
    }

    public SocketPool(OioHttpSettings settings, InetSocketAddress target,
            boolean selfCleaning) {
        this.settings = settings;
        this.leased = new AtomicInteger(0);
        this.target = target;
        this.q = new ArrayBlockingQueue<PooledSocket>(
                settings.pooling().maxPerRoute());
        if (selfCleaning) {
            this.selfCleaner = new SelfCleaner();
            this.selfCleaner.start();
        }
    }

    public void shutdown() {
        if (null != selfCleaner)
            selfCleaner.interrupt();
        Iterator<PooledSocket> it = q.iterator();
        while (it.hasNext())
            it.next().quietClose();
    }

    public PooledSocket lease() {
        PooledSocket sock = q.poll();
        if (null == sock) {
            sock = tryCreate();
            if (null == sock) {
                try {
                    sock = q.poll(settings.pooling().maxWait(),
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

        leased.incrementAndGet();
        return sock.markUnpooled();
    }

    public SocketPool release(PooledSocket sock) {
        leased.decrementAndGet();
        if (sock.isInputShutdown()
                || !q.offer(sock.lastUsage(System.currentTimeMillis()))) {
            sock.quietClose();
        }
        return this;
    }

    public int size() {
        return q.size();
    }

    public int leased() {
        return leased.get();
    }

    private PooledSocket tryCreate() {
        return canCreate() ? create() : null;
    }

    private boolean canCreate() {
        return 0 < settings.pooling().maxPerRoute() - leased.get();
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
                    if (!q.offer(p))
                        p.quietClose();
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
                    clean();
                    sleep(settings.pooling().cleanRate() * 1000);
                }
            } catch (InterruptedException e) {
                logger.debug("Pool cleaner thread interrupted");
            }
        }
    }
}
