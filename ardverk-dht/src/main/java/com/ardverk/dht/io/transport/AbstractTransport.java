package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ardverk.dht.io.session.SessionContext;

public abstract class AbstractTransport implements Transport {

    private final List<TransportListener> listeners 
        = new CopyOnWriteArrayList<TransportListener>();
    
    @Override
    public void send(SocketAddress dst, byte[] message) throws IOException {
        send(dst, message, 0, message.length);
    }
    
    @Override
    public void addTransportListener(TransportListener l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        listeners.add(l);
    }
    
    @Override
    public void removeTransportListener(TransportListener l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        listeners.remove(l);
    }
    
    @Override
    public TransportListener[] getTransportListeners() {
        return listeners.toArray(new TransportListener[0]);
    }
    
    /**
     * 
     */
    protected void received(SessionContext session, Object message) throws IOException {
        for (TransportListener listener : listeners) {
            listener.received(session, message);
        }
    }
}
