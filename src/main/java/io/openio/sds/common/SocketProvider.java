package io.openio.sds.common;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public interface SocketProvider {

    public Socket getSocket(String host, int port);
    public Socket getSocket(InetSocketAddress addr);

    public boolean reusableSocket();

}
