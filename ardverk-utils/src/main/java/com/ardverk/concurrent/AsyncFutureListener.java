package com.ardverk.concurrent;

public interface AsyncFutureListener<V> {

    public void operationComplete(AsyncFuture<V> future);
}
