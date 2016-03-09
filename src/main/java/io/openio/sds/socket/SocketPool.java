package io.openio.sds.socket;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPool {

    private ConcurrentHashMap<SocketAddress, ConcurrentLinkedQueue<PooledSocket>> pool;

    public SocketPool(boolean pooling) {
        if (pooling)
            pool = new ConcurrentHashMap<SocketAddress, ConcurrentLinkedQueue<PooledSocket>>();
    }

    public PooledSocket lease(SocketAddress target) {
        return getSocket(getQueue(target));
    }

    public void release(PooledSocket sock) {

    }

    private PooledSocket getSocket(ConcurrentLinkedQueue<PooledSocket> queue) {
        // TODO Auto-generated method stub
        return null;
    }

    private ConcurrentLinkedQueue<PooledSocket> getQueue(SocketAddress target) {
        ConcurrentLinkedQueue<PooledSocket> q = pool.get(target);
        if(null == q) {
            synchronized(pool) {}
        }
    }

}
