package com.ardverk.concurrent;

public interface AsyncProcess<V> {

    public void start(AsyncFuture<V> future) throws Exception;
}
