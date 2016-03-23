package io.openio.sds.common;

import java.net.Socket;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class SocketProviders {

    public static SocketProvider pooledSocketProvider(PoolSettings pool, OioHttpSettings settings, InetSocketAddress target){
        
        return new SocketProvider() {
            
            @Override
            public boolean reusableSocket() {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public Socket getSocket(String host, int port) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
    
}
