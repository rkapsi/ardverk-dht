/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcessFuture;
import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.lang.TimeStamp;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;

/**
 * An abstract base class for {@link ResponseHandler}s.
 */
abstract class AbstractResponseHandler<V extends Entity> 
        extends AbstractMessageHandler implements ResponseHandler<V> {
    
    protected volatile AsyncProcessFuture<V> future = null;
    
    private final AtomicBoolean done = new AtomicBoolean(false);
    
    private volatile TimeStamp lastSendTime = null;
    
    private volatile TimeStamp lastResponseTime = null;
    
    public AbstractResponseHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    /**
     * Returns the mount of time that has passed since the last message
     * has been sent or -1 if no messages have been sent yet.
     */
    public long getLastSendTime(TimeUnit unit) {
        TimeStamp lastSendTime = this.lastSendTime;
        return lastSendTime != null ? lastSendTime.getAge(unit) : -1L;
    }
    
    /**
     * Returns the amount of time that has passed in milliseconds
     * since the last message has been sent or -1 if no messages
     * have been sent yet.
     */
    public long getLastSendTimeInMillis() {
        return getLastSendTime(TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the mount of time that has passed since the last message
     * has been received or -1 if no messages have been received yet.
     */
    public long getLastResponseTime(TimeUnit unit) {
        TimeStamp lastResponseTime = this.lastResponseTime;
        return lastResponseTime != null ? lastResponseTime.getAge(unit) : -1L;
    }
    
    /**
     * Returns the amount of time that has passed in milliseconds
     * since the last message has been received or -1 if no messages
     * have been received yet.
     */
    public long getLastResponseTimeInMillis() {
        return getLastResponseTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean isOpen() {
        AsyncFuture<V> future = this.future;
        return future != null && !future.isDone();
    }

    /**
     * Returns {@code true} if the underlying {@link AsyncFuture} is
     * done.
     * 
     * NOTE: This method is throwing an {@link IllegalStateException}
     * if the {@link AbstractRequestHandler} isn't initialized yet.
     */
    protected boolean isDone() {
        AsyncFuture<V> future = this.future;
        if (future != null) {
            return future.isDone();
        }
        throw new IllegalStateException();
    }

    /**
     * Sets the value of the underlying {@link AsyncFuture}.
     * 
     * NOTE: This method is throwing an {@link IllegalStateException}
     * if the {@link AbstractRequestHandler} isn't initialized yet.
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
     * Sets the exception of the underlying {@link AsyncFuture}.
     * 
     * NOTE: This method is throwing an {@link IllegalStateException}
     * if the {@link AbstractRequestHandler} isn't initialized yet.
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
     * Sends a {@link RequestMessage} to the given {@link Contact}.
     */
    public void send(Contact dst, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        KUID contactId = dst.getId();
        send(contactId, message, timeout, unit);
    }
    
    /**
     * Sends a {@link RequestMessage} to the given {@link KUID}.
     * 
     * NOTE: The receiver's {@link SocketAddress} is encoded in 
     * the {@link RequestMessage}.
     */
    public void send(KUID contactId, RequestMessage message, 
            long timeout, TimeUnit unit) throws IOException {
        
        if (isOpen()) {
            messageDispatcher.send(this, contactId, 
                    message, timeout, unit);
            lastSendTime = TimeStamp.now();
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
    
    /**
     * Called by {@link #start(AsyncProcessFuture)}.
     * 
     * NOTE: A lock on the given {@link AsyncFuture} is being held.
     */
    protected abstract void go(AsyncFuture<V> future) throws Exception;
    
    /**
     * Called when the underlying {@link AsyncFuture} is done.
     */
    protected void done() {
    }
    
    @Override
    public void handleResponse(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        synchronized (future) {
            if (!isOpen()) {
                return;
            }
            
            synchronized (this) {
                lastResponseTime = TimeStamp.now();
                processResponse(entity, response, time, unit);
            }
        }
    }
    
    /**
     * @see #handleResponse(RequestEntity, ResponseMessage, long, TimeUnit).
     */
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
    
    /**
     * @see #handleTimeout(RequestEntity, long, TimeUnit)
     */
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
    
    /**
     * @see #handleException(RequestEntity, Throwable)
     */
    protected void processException(RequestEntity entity, Throwable exception) {
    }
}