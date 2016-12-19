package io.openio.sds.pool;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.logging.SdsLogger;
import io.openio.sds.logging.SdsLoggerFactory;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public abstract class Pool<T extends Poolable> {

    private static final SdsLogger logger = SdsLoggerFactory.getLogger(Pool.class);

    private ArrayBlockingQueue<T> q;
    private PoolingSettings settings;
    private AtomicInteger leased;
    private Thread selfCleaner;

    public Pool(PoolingSettings settings) {
        this(settings, false);
    }

    public Pool(PoolingSettings settings, boolean selfCleaning) {
        this.settings = settings;
        this.leased = new AtomicInteger(0);
        this.q = new ArrayBlockingQueue<T>(settings.maxForEach());
        if (selfCleaning) {
            this.selfCleaner = new SelfCleaner();
            this.selfCleaner.start();
        }
    }

    public void shutdown() {
        if (null != selfCleaner)
            selfCleaner.interrupt();
        Iterator<T> it = q.iterator();
        while (it.hasNext())
            destroy(it.next());
    }

    /**
     * @return Monotonic (always increasing) number of milliseconds since
     *         "some fixed but arbitrary origin time".
     */
    public static long monotonicMillis() {
        return System.nanoTime() / 1000000;
    }

    protected boolean timedOut(T item, long now) {
        return now >= item.lastUsage() + settings.idleTimeout();
    }

    /**
     * @return the first item of the queue that has not timed out.
     */
    protected T leaseLoop() {
        long now = monotonicMillis();
        T item = q.poll();
        while (item != null && this.timedOut(item, now)) {
            destroy(item);
            item = q.poll();
        }
        return item;
    }

    public T lease() {
        T item = leaseLoop();
        if (item == null) {
            item = tryCreate();
            if (item == null) {
                try {
                    item = q.poll(settings.maxWait(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.debug("connection wait interrrupted");
                }
                if (item == null)
                    throw new OioException(String.format("Unable to get pooled element"));
            }
        }

        leased.incrementAndGet();
        item.setPooled(false);
        return item;
    }

    public Pool<T> release(T item) {
        if (item.isPooled())
            return this;
        leased.decrementAndGet();
        item.lastUsage(monotonicMillis());
        if (!item.reusable() || !q.offer(item)) {
            destroy(item);
        }
        return this;
    }

    public int size() {
        return q.size();
    }

    public int leased() {
        return leased.get();
    }

    protected abstract T create();

    protected abstract void destroy(T t);

    private T tryCreate() {
        return canCreate() ? create() : null;
    }

    private boolean canCreate() {
        return 0 < settings.maxForEach() - leased.get();
    }

    /**
     * Clean all timed out items of the pool. Also cleans the first non timed
     * out item.
     */
    void clean() {
        T item = leaseLoop();
        if (item != null)
            destroy(item);
    }

    private class SelfCleaner extends Thread {

        SelfCleaner() {
            super("cleaner");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                sleep(settings.cleanDelay() * 1000);
                while (true) {
                    clean();
                    sleep(settings.cleanRate() * 1000);
                }
            } catch (InterruptedException e) {
                logger.debug("Pool cleaner thread interrupted");
            }
        }
    }
}
