package com.ardverk.dht.concurrent;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessFutureTask;

import com.ardverk.utils.EventUtils;

public class ArdverkFutureTask<V> extends AsyncProcessFutureTask<V> 
        implements ArdverkRunnableFuture<V> {

    public ArdverkFutureTask() {
        super();
    }

    public ArdverkFutureTask(AsyncProcess<V> process, long timeout,
            TimeUnit unit) {
        super(process, timeout, unit);
    }

    public ArdverkFutureTask(AsyncProcess<V> process) {
        super(process);
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
                ArdverkFutureTask.super.fireOperationComplete(first, others);
            }
        };
        
        EventUtils.fireEvent(event);
    }
}
