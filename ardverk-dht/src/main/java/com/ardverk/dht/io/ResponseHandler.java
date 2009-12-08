package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.utils.Checkable;

public abstract class ResponseHandler<V extends Entity> 
        extends AbstractMessageHandler implements MessageCallback, Checkable, AsyncProcess<V> {
    
    private volatile AsyncFuture<V> future = null;
    
    public ResponseHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
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
            return;
        }
        throw new IllegalStateException();
    }
    
    protected void setException(Throwable t) {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            future.setException(t);
            return;
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
    
    protected abstract void innerStart(
            AsyncFuture<V> future) throws Exception;
    
    /**
     * 
     */
    public abstract void handleTimeout(RequestMessage request, long time, TimeUnit unit) throws IOException;
}
