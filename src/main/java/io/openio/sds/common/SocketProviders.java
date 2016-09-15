package io.openio.sds.common;

import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.http.SocketPool;
import io.openio.sds.pool.PoolingSettings;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 */
public class SocketProviders {

    /**
     * Socket provider that reuses socket instances if possible.
     *
     * @param target
     *            The address that will be requested most of the time. A request
     *            to another address will create a new socket instead of taking
     *            it from the pool.
     */
    public static SocketProvider pooledSocketProvider(PoolingSettings poolSettings,
            final OioHttpSettings httpSettings, final InetSocketAddress target) {

        final SocketPool pool = new SocketPool(httpSettings, poolSettings, target);
        return new AbstractSocketProvider() {

            @Override
            public boolean reusableSocket() {
                return true;
            }

            @Override
            public Socket getSocket(String host, int port) {
                return getSocket(new InetSocketAddress(host, port));
            }

            @Override
            public Socket getSocket(InetSocketAddress addr) {
                if (!addr.equals(target)) {
                    Socket sock = new Socket();
                    configureAndConnect(sock, addr, httpSettings);
                    return sock;
                } else {
                    return pool.lease();
                }
            }
        };
    }

    public static SocketProvider directSocketProvider(final OioHttpSettings http) {
        return new AbstractSocketProvider() {

            @Override
            public boolean reusableSocket() {
                return false;
            }

            @Override
            public Socket getSocket(String host, int port) {
                InetSocketAddress target = new InetSocketAddress(host, port);
                return getSocket(target);
            }

            @Override
            public Socket getSocket(InetSocketAddress target) {
                Socket sock = new Socket();
                configureAndConnect(sock, target, http);
                return sock;
            }
        };
    }
}
