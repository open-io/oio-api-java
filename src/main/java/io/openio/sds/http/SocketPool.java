package io.openio.sds.http;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.pool.Pool;
import io.openio.sds.pool.PoolingSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketPool extends Pool<PooledSocket> {

    private OioHttpSettings settings;
    private InetSocketAddress target;

    public SocketPool(OioHttpSettings settings, PoolingSettings pooling,
            InetSocketAddress target) {
        super(pooling);
        this.settings = settings;
        this.target = target;
    }

    @Override
    protected PooledSocket create() {
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

    @Override
    protected void destroy(PooledSocket p) {
        p.quietClose();
    }

}
