package com.ardverk.concurrent;

public interface AsyncProcess<V> {

    /**
     * NOTE: This method is being called while a lock on 'future'
     * is being held!
     */
    public void start(AsyncFuture<V> future) throws Exception;
}
