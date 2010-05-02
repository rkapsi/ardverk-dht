package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 */
public interface TransportCallback {
    
    /**
     * 
     */
    public void received(SocketAddress src, byte[] message, 
            int offset, int length) throws IOException;
}