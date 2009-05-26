package com.ardverk.dht.io;

import com.ardverk.concurrent.AsyncFuture;
import com.ardverk.concurrent.AsyncProcess;
import com.ardverk.utils.Checkable;

public abstract class MessageHandler<V> implements Checkable, AsyncProcess<V> {
    
    private volatile AsyncFuture<V> future = null;
    
    @Override
    public boolean isOpen() {
        AsyncFuture<V> future = this.future;
        return future != null && !future.isDone();
    }

    @Override
    public final void start(AsyncFuture<V> future) throws Exception {
        if (future == null) {
            throw new NullPointerException("future");
        }
        
        this.future = future;
        innerStart(future);
    }
    
    protected abstract void innerStart(AsyncFuture<V> future) throws Exception;
}
