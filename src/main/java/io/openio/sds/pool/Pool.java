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
 *
 */
public abstract class Pool<T extends Poolable> {

	private static final SdsLogger logger = SdsLoggerFactory
	        .getLogger(Pool.class);

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
		this.q = new ArrayBlockingQueue<T>(
		        settings.maxForEach());
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

	public T lease() {
		T item = q.poll();
		if (null == item) {
			item = tryCreate();
			if (null == item) {
				try {
					item = q.poll(settings.maxWait(),
					        TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug("connection wait interrrupted");
				}
				if (null == item)
					throw new OioException(
					        String.format("Unable to get pooled element"));
			}
		}

		leased.incrementAndGet();
		item.setPooled(false);
		return item;
	}

	public Pool<T> release(T item) {
		if(item.isPooled())
			return this;
		leased.decrementAndGet();
		item.lastUsage(System.currentTimeMillis());
		if (!item.reusable()
		        || !q.offer(item)) {
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

	void clean() {
		long t = System.currentTimeMillis();
		T p = q.peek();
		if (null == p)
			return;
		boolean stop = false;
		if (t - p.lastUsage() > settings.idleTimeout()) {
			while (!stop && null != (p = q.poll())) {
				if (t - p.lastUsage() > settings.idleTimeout()) {
					destroy(p);
				} else {
					stop = true;
					if (!q.offer(p))
						destroy(p);
				}
			}
		}
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
