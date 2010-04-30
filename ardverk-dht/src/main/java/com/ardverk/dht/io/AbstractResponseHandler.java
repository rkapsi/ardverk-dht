package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcessFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact2;

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
    public void send(Contact2 dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getContactId();
        SocketAddress addr = dst.getContactAddress();
        
        send(contactId, addr, message, timeout, unit);
    }
    
    public void send(SocketAddress addr, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        send(null, addr, message, timeout, unit);
    }
    
    public void send(KUID contactId, SocketAddress addr, 
            RequestMessage message, long timeout, TimeUnit unit) throws IOException {
        
        if (isOpen()) {
            messageDispatcher.send(this, contactId, addr, 
                    message, timeout, unit);
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
    public void handleResponse(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                lastResponseTime = System.currentTimeMillis();
                processResponse(entity, response, time, unit);
            }
        }
    }
    
    protected abstract void processResponse(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException;

    @Override
    public void handleTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException {
        
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                processTimeout(entity, time, unit);                
            }
        }
    }
    
    protected abstract void processTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException;

    @Override
    public void handleException(RequestEntity entity, Throwable exception) {
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                processException(entity, exception);                
            }
        }
    }
    
    protected void processException(RequestEntity entity, Throwable exception) {
        
    }
}
