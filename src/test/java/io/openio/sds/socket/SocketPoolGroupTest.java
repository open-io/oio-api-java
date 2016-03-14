package io.openio.sds.socket;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.openio.sds.http.OioHttpSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPoolGroupTest {

    private static SocketPoolGroup pool;

    @Before
    public void beforeEach() {
        pool = new SocketPoolGroup(new OioHttpSettings());
    }

    @After
    public void afterEach() {
        pool.shutdown();
    }

    @Test
    public void leaseAndRelease() throws InterruptedException, IOException {
        PooledSocket sock = pool.lease("127.0.0.1", 6002);
        sock.close();
        Assert.assertEquals(1, pool.size("127.0.0.1", 6002));
        Thread.sleep(5000);
        Assert.assertEquals(0, pool.size("127.0.0.1", 6002));
    }
}
