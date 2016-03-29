package io.openio.sds.socket;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.pool.Pool;
import io.openio.sds.pool.Poolable;
import io.openio.sds.pool.PoolingSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PoolTest {

	private Pool<Poolable> pool;

	@Before
	public void before() {
		PoolingSettings settings = new PoolingSettings();
		settings.cleanDelay(1L)
		        .cleanRate(1L)
		        .idleTimeout(500)
		        .maxForEach(20)
		        .maxWait(100);
		pool = new Pool<Poolable>(settings, true) {

			@Override
			protected Poolable create() {
				return new Poolable() {

					private boolean pooled = false;
					private long lastUsage;

					@Override
					public boolean reusable() {
						return true;
					}

					@Override
					public void lastUsage(long lastUsage) {
						this.lastUsage = lastUsage;
					}

					@Override
					public long lastUsage() {
						return this.lastUsage;
					}

					@Override
					public void setPooled(boolean pooled) {
						this.pooled = pooled;
					}

					@Override
					public boolean isPooled() {
						return pooled;
					}
				};
			}

			@Override
			protected void destroy(Poolable t) {
				// nothing
			}

		};
	}

	@After
	public void after() {
		pool.shutdown();
	}

	@Test
	public void leaseRelease() throws IOException, InterruptedException {
		Poolable p = pool.lease();
		pool.release(p);
		Assert.assertEquals(1, pool.size());
		Thread.sleep(3000);
		Assert.assertEquals(0, pool.size());
	}

	@Test
	public void reachMax() {
		for (int i = 0; i < 20; i++)
			pool.lease();
		try {
			pool.lease();
			Assert.fail();
		} catch (OioException e) {
			Assert.assertTrue(
			        e.getMessage().contains("Unable to get pooled element"));
		}
	}

	@Test
	public void multiThread() throws InterruptedException {
		Thread[] threads = new Thread[10];
		for (int i = 0; i < 10; i++) {
			threads[i] = new Thread() {
				@Override
				public void run() {
					for (int j = 0; j < 1000; j++) {
						Poolable p = pool.lease();
						try {
							Thread.sleep(10);
							pool.release(p);
						} catch (Exception e) {
							e.printStackTrace();
							Assert.fail(e.getMessage());
						}
					}
				}
			};
		}
		for (int i = 0; i < 10; i++)
			threads[i].start();
		for (int i = 0; i < 10; i++)
			threads[i].join();
		Thread.sleep(4000);
		Assert.assertEquals(0, pool.size());
		Assert.assertEquals(0, pool.leased());
	}

}
