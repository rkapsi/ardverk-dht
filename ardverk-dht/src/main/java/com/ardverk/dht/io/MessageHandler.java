package com.ardverk.dht.io;

import com.ardverk.concurrent.AsyncFuture;
import com.ardverk.concurrent.AsyncFutureListener;
import com.ardverk.concurrent.AsyncProcess;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.utils.Checkable;

public abstract class MessageHandler<V, M extends ResponseMessage> implements Checkable, AsyncProcess<V> {
    
    private volatile AsyncFuture<V> future = null;
    
    @Override
    public boolean isOpen() {
        AsyncFuture<V> future = this.future;
        return future != null && !future.isDone();
    }

    protected boolean isDone() {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            return future.isDone();
        }
        throw new IllegalStateException();
    }

    protected void setValue(V value) {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            future.setValue(value);
        }
        throw new IllegalStateException();
    }
    
    protected void setException(Throwable t) {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            future.setException(t);
        }
        throw new IllegalStateException();
    }
    
    @Override
    public final void start(AsyncFuture<V> future) throws Exception {
        if (future == null) {
            throw new NullPointerException("future");
        }
        
        AsyncFutureListener<V> listener = new AsyncFutureListener<V>() {
            @Override
            public void operationComplete(AsyncFuture<V> future) {
                // KILL NETWORK TASK
            }
        };
        future.addAsyncFutureListener(listener);
        
        this.future = future;
        innerStart(future);
    }
    
    protected abstract void innerStart(AsyncFuture<V> future) throws Exception;
    
    protected abstract void handleMessage(M message) throws Exception;
}
