package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcessFuture;

import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public abstract class AbstractResponseHandler<V extends Entity> 
        extends AbstractMessageHandler implements ResponseHandler<V> {
    
    protected volatile AsyncProcessFuture<V> future = null;
    
    private final AtomicBoolean done = new AtomicBoolean(false);
    
    private volatile long lastSendTime = -1L;
    
    private volatile long lastResponseTime = -1L;
    
    public AbstractResponseHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    /**
     * 
     */
    public long getLastSendTime(TimeUnit unit) {
        return convertTime(lastSendTime, unit);
    }
    
    /**
     * 
     */
    public long getLastResponseTime(TimeUnit unit) {
        return convertTime(lastResponseTime, unit);
    }
    
    /**
     * 
     */
    private long convertTime(long timeInMillis, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        if (timeInMillis == -1L) {
            return -1L;
        }
        
        long time = System.currentTimeMillis() - timeInMillis;
        return unit.convert(time, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean isOpen() {
        AsyncFuture<V> future = this.future;
        return future != null && !future.isDone();
    }

    /**
     * 
     */
    protected boolean isDone() {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            return future.isDone();
        }
        throw new IllegalStateException();
    }

    /**
     * 
     */
    protected void setValue(V value) {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            future.setValue(value);
            if (!done.getAndSet(true)) {
                done();
            }
            return;
        }
        throw new IllegalStateException();
    }
    
    /**
     * 
     */
    protected void setException(Throwable t) {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            future.setException(t);
            if (!done.getAndSet(true)) {
                done();
            }
            return;
        }
        throw new IllegalStateException();
    }
    
    /**
     * 
     */
    public void send(RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        if (isOpen()) {
            messageDispatcher.send(this, message, timeout, unit);
            lastSendTime = System.currentTimeMillis();
        }
    }
    
    @Override
    public final void start(AsyncProcessFuture<V> future) throws Exception {
        if (future == null) {
            throw new NullPointerException("future");
        }
        
        this.future = future;
        go(future);
        
        future.addAsyncFutureListener(new AsyncFutureListener<V>() {
            @Override
            public void operationComplete(AsyncFuture<V> future) {
                if (!done.getAndSet(true)) {
                    done();
                }
            }
        });
    }
    
    protected void done() {
    }
    
    protected abstract void go(AsyncFuture<V> future) throws Exception;
    
    
    @Override
    public void handleResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                lastResponseTime = System.currentTimeMillis();
                processResponse(request, response, time, unit);
            }
        }
    }
    
    protected abstract void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException;

    @Override
    public void handleTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                processTimeout(request, time, unit);                
            }
        }
    }
    
    protected abstract void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException;

    @Override
    public void handleException(RequestMessage request, Throwable exception) {
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                processException(request, exception);                
            }
        }
    }
    
    protected void processException(RequestMessage request, Throwable exception) {
        
    }
}
