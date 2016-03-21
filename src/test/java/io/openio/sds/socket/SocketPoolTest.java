package io.openio.sds.socket;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPoolTest {

    private SocketPool pool;

    @Before
    public void before() {
        OioHttpSettings settings = new OioHttpSettings();
        settings.pooling().cleanDelay(1L)
                .cleanRate(1L)
                .socketIdleTimeout(500)
                .maxPerRoute(20)
                .maxWait(100);
        pool = new SocketPool(settings,
                new InetSocketAddress("127.0.0.1", 6002),
                true);
    }

    @After
    public void after() {
        pool.shutdown();
    }

    @Test
    public void leaseRelease() throws IOException, InterruptedException {
        PooledSocket sock = pool.lease();
        sock.close();
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
                    e.getMessage().contains("Unable to get connection"));
        }
    }

    @Test
    public void multiThread() throws InterruptedException {
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 50; j++) {
                        PooledSocket sock = pool.lease();
                        try {
                            Thread.sleep(10);
                            sock.close();
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
