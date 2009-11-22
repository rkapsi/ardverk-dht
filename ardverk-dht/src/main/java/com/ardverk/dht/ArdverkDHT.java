package com.ardverk.dht;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.io.transport.TransportListener;

public class ArdverkDHT implements Closeable {

    private final TransportListener listener = new Foo();
    
    private final NodeManager nodeManager 
        = new NodeManager();
    
    private final Transport transport;
    
    public ArdverkDHT(Transport transport, KUID nodeId) {
        if (transport == null) {
            throw new NullPointerException("transport");
        }
        
        if (nodeId == null) {
            throw new NullPointerException("nodeId");
        }
        
        this.transport = transport;
        nodeManager.add(nodeId);
        
        transport.addTransportListener(listener);
    }
    
    @Override
    public void close() {
        transport.removeTransportListener(listener);
    }
    
    public Transport getTransport() {
        return transport;
    }
    
    private class Foo implements TransportListener {

        @Override
        public void received(SocketAddress src, Object message)
                throws IOException {
        }
    }
}
