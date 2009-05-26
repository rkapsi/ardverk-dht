package com.ardverk.concurrent;

import java.util.concurrent.Future;

public interface AsyncFuture<V> extends Future<V> {

    public void addAsyncFutureListener(AsyncFutureListener<V> l);
    
    public void removeAsyncFutureListener(AsyncFutureListener<V> l);
    
    public AsyncFutureListener<V>[] getAsyncFutureListeners();
}
