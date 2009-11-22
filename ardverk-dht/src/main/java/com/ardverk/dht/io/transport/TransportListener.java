package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;

public interface TransportListener {
    
    public void received(SocketAddress src, Object message) throws IOException;
}