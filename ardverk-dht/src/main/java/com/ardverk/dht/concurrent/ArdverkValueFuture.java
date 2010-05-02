package com.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncValueFuture;

import com.ardverk.utils.EventUtils;

public class ArdverkValueFuture<V> extends AsyncValueFuture<V> 
        implements ArdverkFuture<V> {

    public ArdverkValueFuture() {
        super();
    }

    public ArdverkValueFuture(Throwable t) {
        super(t);
    }

    public ArdverkValueFuture(V value) {
        super(value);
    }

    @Override
    public long getTimeout(TimeUnit unit) {
        return 0;
    }

    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTimeout() {
        return false;
    }

    @Override
    protected boolean isEventThread() {
        return EventUtils.isEventThread();
    }
    
    @Override
    protected void fireOperationComplete(final AsyncFutureListener<V> first,
            final AsyncFutureListener<V>... others) {
        
        if (first == null) {
            return;
        }
        
        Runnable event = new Runnable() {
            @Override
            public void run() {
                ArdverkValueFuture.super.fireOperationComplete(first, others);
            }
        };
        
        EventUtils.fireEvent(event);
    }
}
