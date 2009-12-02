package com.ardverk.dht.io.process;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher.Callback;

abstract class AbstractProcess<T> implements AsyncProcess<T>, Callback {

    protected final MessageDispatcher messageDispatcher;
    
    private volatile AsyncFuture<T> future;
    
    public AbstractProcess(MessageDispatcher messageDispatcher) {
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        
        this.messageDispatcher = messageDispatcher;
    }
    
    protected AsyncFuture<T> getAsyncFuture() {
        AsyncFuture<T> future = this.future;
        if (future == null) {
            throw new IllegalStateException();
        }
        return future;
    }
    
    @Override
    public final void start(AsyncFuture<T> future) throws Exception {
        this.future = future;
        start0(future);
    }

    protected abstract void start0(AsyncFuture<T> future) throws Exception;
}
