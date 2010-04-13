package com.ardverk.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncFutureTask;
import org.ardverk.concurrent.ExecutorUtils;

import com.ardverk.utils.EventUtils;

public class AsyncProcessFutureTask<V> extends AsyncFutureTask<V> 
        implements AsyncProcessRunnableFuture<V> {

    private static final ScheduledThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor("WatchdogThread");
    
    private static final AsyncProcess<Object> DEFAULT 
            = new AsyncProcess<Object>() {        
        @Override
        public void start(AsyncProcessFuture<Object> future) {
            throw new IllegalStateException();
        }
    };
    
    private final AsyncProcess<V> process;
    
    private final long timeout;
    
    private final TimeUnit unit;
    
    private ScheduledFuture<?> future = null;
    
    private boolean wasTimeout = false;
    
    @SuppressWarnings("unchecked")
    public AsyncProcessFutureTask() {
        this((AsyncProcess<V>)DEFAULT);
    }
    
    public AsyncProcessFutureTask(AsyncProcess<V> process) {
        this(process, -1L, TimeUnit.MILLISECONDS);
    }
    
    public AsyncProcessFutureTask(AsyncProcess<V> process, 
            long timeout, TimeUnit unit) {
        this.process = process;
        this.timeout = timeout;
        this.unit = unit;
    }
    
    @Override
    protected synchronized void doRun() throws Exception {
        if (!isDone()) {
            start();
            watchdog();
        }
    }
    
    protected synchronized void start() throws Exception {
        process.start(this);
    }
    
    private synchronized void watchdog() {
        if (timeout == -1L) {
            return;
        }
        
        if (isDone()) {
            return;
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (AsyncProcessFutureTask.this) {
                    if (!isDone()) {
                        wasTimeout = true;
                        setException(new TimeoutException());
                    }
                }
            }
        };
        
        future = EXECUTOR.schedule(task, timeout, unit);
    }
    
    @Override
    public long getTimeout(TimeUnit unit) {
        return unit.convert(timeout, this.unit);
    }
    
    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS); 
    }
    
    @Override
    public synchronized boolean isTimeout() {
        return wasTimeout;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        checkIfEventThread();
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        checkIfEventThread();
        return super.get(timeout, unit);
    }

    @Override
    protected final void done() {
        synchronized (this) {
            if (future != null) {
                future.cancel(true);
            }
        }
        
        super.done();
        done0();
    }
    
    protected void done0() {
        
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
                AsyncProcessFutureTask.super.fireOperationComplete(first, others);
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    private void checkIfEventThread() {
        if (!isDone() && EventUtils.isEventThread()) {
            throw new IllegalStateException();
        }
    }
}
