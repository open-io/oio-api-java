package io.openio.sds.common;

import java.net.Socket;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public interface SocketProvider {

    public Socket getSocket(String host, int port);

    public boolean reusableSocket();

}
