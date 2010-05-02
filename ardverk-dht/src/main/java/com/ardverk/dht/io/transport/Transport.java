package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 */
public interface Transport {

    /**
     * 
     */
    public void bind(TransportCallback callback) throws IOException;
    
    /**
     * 
     */
    public void unbind();
    
    /**
     * 
     */
    public boolean isBound();
    
    /**
     * 
     */
    public void send(SocketAddress dst, byte[] message)
            throws IOException;

    /**
     * 
     */
    public void send(SocketAddress dst, byte[] message, int offset,
            int length) throws IOException;
}