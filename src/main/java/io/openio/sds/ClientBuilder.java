package io.openio.sds;

import static io.openio.sds.http.OioHttp.http;
import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

import io.openio.sds.common.SocketProvider;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttp;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.http.SocketPool;
import io.openio.sds.pool.PoolingSettings;
import io.openio.sds.proxy.ProxyClient;
import io.openio.sds.rawx.RawxClient;

/**
 * Builder for @link {@link Client} implementations
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ClientBuilder {

    /**
     * Creates a clien using the specified settings
     * @param settings the settings to use
     * @return The build {@link Client}
     */
    public static DefaultClient newClient(Settings settings) {
        OioHttp proxyHttp = http(settings.proxy().http(),
                proxySocketProvider(
                        settings.proxy().url(), settings.proxy().http(),
                        settings.proxy().pooling()));
        OioHttp rawxHttp = http(settings.rawx().http(),
                rawxSocketProvider(settings.rawx().http()));
        ProxyClient proxy = new ProxyClient(proxyHttp, settings.proxy());
        RawxClient rawx = new RawxClient(rawxHttp, settings.rawx());
        return new DefaultClient(proxy, rawx);
    }

    /**
     * Creates a client without specific configuration. Useful for testing
     * purpose
     *
     * @param ns
     *            the OpenIO Namespace
     * @param proxydUrl
     *            the url of OpenIO proxyd service
     * @return The build {@link Client}
     */
    public static DefaultClient newClient(String ns, String proxydUrl) {
        Settings settings = new Settings();
        settings.proxy().url(proxydUrl);
        settings.proxy().ns(ns);
        return newClient(settings);
    }

    private static SocketProvider proxySocketProvider(String url,
            final OioHttpSettings http, PoolingSettings pooling) {
        URI uri = URI.create(url);
        final InetSocketAddress target = new InetSocketAddress(uri.getHost(),
                uri.getPort());
        if (pooling.enabled()) {
            final SocketPool pool = new SocketPool(http, pooling, target);
            return new SocketProvider() {

                @Override
                public boolean reusableSocket() {
                    return true;
                }

                @Override
                public Socket getSocket(String host, int port) {
                    return pool.lease();
                }
            };

        }

        return new SocketProvider() {

            @Override
            public boolean reusableSocket() {
                return false;
            }

            @Override
            public Socket getSocket(String host, int port) {
                InetSocketAddress target = new InetSocketAddress(host, port);
                try {
                    Socket sock = new Socket();
                    sock.setSendBufferSize(http.sendBufferSize());
                    sock.setReuseAddress(true);
                    sock.setReceiveBufferSize(http.receiveBufferSize());
                    sock.connect(target, http.connectTimeout());
                    return sock;
                } catch (IOException e) {
                    throw new OioException(format(
                            "Unable to get connection to %s",
                            target.toString()), e);
                }
            }
        };
    }

    private static SocketProvider rawxSocketProvider(
            final OioHttpSettings http) {
        return new SocketProvider() {
            @Override
            public boolean reusableSocket() {
                return false;
            }

            @Override
            public Socket getSocket(String host, int port) {
                InetSocketAddress target = new InetSocketAddress(host, port);
                try {
                    Socket sock = new Socket();
                    sock.setSendBufferSize(http.sendBufferSize());
                    sock.setReuseAddress(true);
                    sock.setReceiveBufferSize(http.receiveBufferSize());
                    sock.connect(target, http.connectTimeout());
                    return sock;
                } catch (IOException e) {
                    throw new OioException(format(
                            "Unable to get connection to %s",
                            target.toString()), e);
                }
            }
        };
    }
}
