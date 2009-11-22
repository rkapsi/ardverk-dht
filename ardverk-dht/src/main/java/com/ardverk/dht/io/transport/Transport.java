package com.ardverk.dht.io.transport;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * 
 */
public interface Transport extends Closeable {

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

    /**
     * 
     */
    public void addTransportListener(TransportListener l);

    /**
     * 
     */
    public void removeTransportListener(TransportListener l);

    /**
     * 
     */
    public TransportListener[] getTransportListeners();
}