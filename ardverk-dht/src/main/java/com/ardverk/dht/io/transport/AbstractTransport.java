package com.ardverk.dht.io.transport;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.lang.NullArgumentException;

public abstract class AbstractTransport implements Transport {
    
    private final AtomicReference<TransportCallback> callbackRef 
        = new AtomicReference<TransportCallback>();
    
    @Override
    public void send(SocketAddress dst, byte[] message) throws IOException {
        send(dst, message, 0, message.length);
    }
    
    @Override
    public void bind(TransportCallback callback) throws IOException {
        if (callback == null) {
            throw new NullArgumentException("callback");
        }
        
        if (!callbackRef.compareAndSet(null, callback)) {
            throw new IOException();
        }
    }
    
    @Override
    public void unbind() {
        callbackRef.set(null);
    }

    @Override
    public boolean isBound() {
        return callbackRef.get() != null;
    }
    
    protected boolean received(SocketAddress src, byte[] message, 
            int offset, int length) throws IOException {
        
        TransportCallback callback = callbackRef.get();
        if (callback != null) {
            callback.received(src, message, offset, length);
            return true;
        }
        return false;
    }
}
