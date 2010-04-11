package com.ardverk.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

public interface AsyncProcessFuture<V> extends AsyncFuture<V> {
    
    public long getTimeout(TimeUnit unit);
    
    public long getTimeoutInMillis();
    
    public boolean isTimeout();
}
