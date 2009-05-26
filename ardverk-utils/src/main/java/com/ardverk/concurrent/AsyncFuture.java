package com.ardverk.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface AsyncFuture<V> extends Future<V> {
    
    public void setValue(V value);
    
    public void setException(Throwable t);
    
    public boolean isTimeout();
    
    public V tryGet() throws InterruptedException, ExecutionException;
    
    public boolean throwsException();
    
    public void addAsyncFutureListener(AsyncFutureListener<V> l);
    
    public void removeAsyncFutureListener(AsyncFutureListener<V> l);
    
    public AsyncFutureListener<V>[] getAsyncFutureListeners();
}
