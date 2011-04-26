package org.ardverk.dht.concurrent;

import org.ardverk.concurrent.AsyncFutureListener;

/**
 * 
 */
public class NopFuture<T> extends DHTValueFuture<T> {
    
    public NopFuture(T value) {
        super(value);
    }

    public NopFuture(Throwable t) {
        super(t);
    }

    @Override
    public void addAsyncFutureListener(AsyncFutureListener<T> l) {
        fireOperationComplete(l);
    }
}