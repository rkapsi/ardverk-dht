package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcessFuture;
import org.ardverk.lang.Arguments;
import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;

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
    
    public long getLastSendTimeInMillis() {
        return getLastSendTime(TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    public long getLastResponseTime(TimeUnit unit) {
        return convertTime(lastResponseTime, unit);
    }
    
    public long getLastResponseTimeInMillis() {
        return getLastResponseTime(TimeUnit.MILLISECONDS);
    }
    
    /**
     * 
     */
    private static long convertTime(long timeInMillis, TimeUnit unit) {
        if (unit == null) {
            throw new NullArgumentException("unit");
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
    public void send(Contact dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getId();
        send(contactId, message, timeout, unit);
    }
    
    public void send(RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        send((KUID)null, message, timeout, unit);
    }
    
    public void send(KUID contactId, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        if (isOpen()) {
            messageDispatcher.send(this, contactId, 
                    message, timeout, unit);
            lastSendTime = System.currentTimeMillis();
        }
    }
    
    @Override
    public final void start(AsyncProcessFuture<V> future) throws Exception {
        this.future = Arguments.notNull(future, "future");
        
        future.addAsyncFutureListener(new AsyncFutureListener<V>() {
            @Override
            public void operationComplete(AsyncFuture<V> future) {
                if (!done.getAndSet(true)) {
                    done();
                }
            }
        });
        
        synchronized (future) {
            if (!future.isDone()) {
                go(future);
            }
        }
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
