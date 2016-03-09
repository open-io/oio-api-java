package io.openio.sds.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class PooledSocket extends Socket {

    private SocketAddress target;
    private SocketPool pool;
    private boolean pooled = false;

    PooledSocket(SocketPool pool) {
        super();
        this.pool = pool;
    }

    @Override
    public void connect(SocketAddress address) throws IOException {
        this.target = address;
        super.connect(address);
    }

    @Override
    public void connect(SocketAddress address, int timeout) throws IOException {
        this.target = address;
        super.connect(address, timeout);
    }

    @Override
    public void close() throws IOException {
        if (!pooled) {
            pool.release(this);
            pooled = true;
        } else {
            super.close();
        }
    }

}
