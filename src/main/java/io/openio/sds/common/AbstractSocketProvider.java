package io.openio.sds.common;

import static java.lang.String.format;
import io.openio.sds.exceptions.OioException;
import io.openio.sds.http.OioHttpSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import static io.openio.sds.common.Check.checkArgument;

/**
 *
 * @author Florent Vennetier
 *
 */
public abstract class AbstractSocketProvider implements SocketProvider {

    /**
     * Configure an already created Socket with provided settings, and establish the connection.
     *
     * @param sock A Socket instance
     * @param target The address to connect the socket to
     * @param http The settings to apply
     * @throws OioException if an error occurs during connection
     */
    protected void configureAndConnect(Socket sock, InetSocketAddress target, OioHttpSettings http)
            throws OioException {
        checkArgument(sock != null, "'sock' argument should not be null");
        checkArgument(target != null, "'target' argument should not be null");
        checkArgument(http != null, "'http' argument should not be null");
        try {
            sock.setReuseAddress(true);
            if (http.setSocketBufferSize()) {
                sock.setSendBufferSize(http.sendBufferSize());
                sock.setReceiveBufferSize(http.receiveBufferSize());
            }
            sock.setSoTimeout(http.readTimeout());
            sock.connect(target, http.connectTimeout());
        } catch (IOException e) {
            throw new OioException(format("Unable to get connection to %s", target.toString()), e);
        }
    }
}
