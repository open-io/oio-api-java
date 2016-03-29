package io.openio.sds.common;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;
import io.openio.sds.http.SocketPool;
import io.openio.sds.pool.PoolingSettings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketProviders {

	public static SocketProvider pooledSocketProvider(PoolingSettings poolSettings, OioHttpSettings httpSettings,
			final InetSocketAddress target) {

		final SocketPool pool = new SocketPool(httpSettings, poolSettings, target);
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
	
	public static SocketProvider directSocketProvider(final OioHttpSettings http) {
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
