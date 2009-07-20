package com.ardverk.concurrent;

/**
 * 
 */
public interface AsyncProcess<V> {

    /**
     * Starts the {@link AsyncProcess}
     * 
     * NOTE: This method is being called while a lock on the given 'future'
     * is being held!
     */
    public void start(AsyncFuture<V> future) throws Exception;
}
