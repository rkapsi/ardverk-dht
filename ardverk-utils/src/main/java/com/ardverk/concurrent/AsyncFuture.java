package com.ardverk.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 
 */
public interface AsyncFuture<V> extends Future<V> {
    
    /**
     * Sets the value of the {@link AsyncFuture}
     */
    public void setValue(V value);
    
    /**
     * Sets the {@link Exception} of the {@link AsyncFuture}
     */
    public void setException(Throwable t);
    
    /**
     * Returns true if the {@link AsyncFuture} finished due to a timeout.
     */
    public boolean isTimeout();
    
    /**
     * A non-blocking version of the {@link #get()} method. It returns
     * either null if the {@link AsyncFuture} is not done yet or the
     * value and throws an Exception respectively.
     */
    public V tryGet() throws InterruptedException, ExecutionException;
    
    /**
     * Returns true if the {@link AsyncFuture} is done and any of 
     * the {@link #get()} methods will throw an {@link Exception}.
     */
    public boolean throwsException();
    
    /**
     * Adds the given {@link AsyncFutureListener}.
     * 
     * NOTE: It's possible to add an {@link AsyncFutureListener} even after the 
     * {@link AsyncFuture} is done and the listener will be notified through
     * the Event Thread. The listener is however not being added to the 
     * {@link AsyncFuture}'s listener list. That means it will not appear in 
     * the list that is being returned by {@link #getAsyncFutureListeners()}.
     */
    public void addAsyncFutureListener(AsyncFutureListener<V> l);
    
    /**
     * Removes the given {@link AsyncFutureListener}
     */
    public void removeAsyncFutureListener(AsyncFutureListener<V> l);
    
    /**
     * Returns all {@link AsyncFutureListener}s
     * 
     * @see Please see {@link #addAsyncFutureListener(AsyncFutureListener)}
     */
    public AsyncFutureListener<V>[] getAsyncFutureListeners();
}
