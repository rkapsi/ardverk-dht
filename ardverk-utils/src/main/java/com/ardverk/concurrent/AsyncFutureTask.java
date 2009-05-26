package com.ardverk.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ardverk.utils.EventUtils;
import com.ardverk.utils.ExecutorUtils;

public class AsyncFutureTask<V> extends FutureTask<V> implements AsyncFuture<V> {

    private static final ScheduledThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newScheduledThreadPool(4, "AsyncFutureTaskThread");
    
    private final List<AsyncFutureListener<V>> listeners 
        = new CopyOnWriteArrayList<AsyncFutureListener<V>>();
    
    private final long timeout;
    
    private final TimeUnit unit;
    
    private ScheduledFuture<?> future = null;
    
    private boolean wasTimeout = false;
    
    public AsyncFutureTask(Callable<V> callable, long timeout, TimeUnit unit) {
        super(callable);
        
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout=" + timeout);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.timeout = timeout;
        this.unit = unit;
    }

    public AsyncFutureTask(Runnable runnable, V result, long timeout, TimeUnit unit) {
        super(runnable, result);
        
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout=" + timeout);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.timeout = timeout;
        this.unit = unit;
    }
    
    @Override
    public void run() {
        initWatchdog();
        super.run();
    }
    
    @Override
    protected boolean runAndReset() {
        initWatchdog();
        return super.runAndReset();
    }

    private synchronized void initWatchdog() {
        if (future != null) {
            future.cancel(true);
        }
        
        if (!isDone()) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    synchronized (AsyncFutureTask.this) {
                        if (!isDone()) {
                            wasTimeout = true;
                            watchdogWillKillFuture();
                            setException(new TimeoutException());
                        }
                    }
                }
            };
            
            wasTimeout = false;
            future = EXECUTOR.schedule(task, timeout, unit);
        }
    }
    
    protected void watchdogWillKillFuture() {
        // OVERRIDE
    }
    
    public synchronized boolean isTimeout() {
        return isDone() && wasTimeout;
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
    public synchronized void addAsyncFutureListener(final AsyncFutureListener<V> l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        if (isDone()) {
            Runnable event = new Runnable() {
                @Override
                public void run() {
                    l.operationComplete(AsyncFutureTask.this);
                }
            };
            EventUtils.fireEvent(event);
        } else {
            listeners.add(l);
        }
    }

    @Override
    public void removeAsyncFutureListener(AsyncFutureListener<V> l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        listeners.remove(l);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public AsyncFutureListener<V>[] getAsyncFutureListeners() {
        return listeners.toArray(new AsyncFutureListener[0]);
    }
    
    @Override
    protected final void done() {
        super.done();
        
        synchronized (this) {
            if (future != null) {
                future.cancel(true);
            }
            
            fireOperationComplete();
        }
        
        innerDone();
    }
    
    protected void innerDone() {
        // OVERRIDE
    }

    /**
     * Make sure we're not calling get() from the EventThread
     * which is fatal and a PITA to debug as it's in some cases
     * not obvious why it's failing!
     */
    private void checkIfEventThread() throws IllegalStateException {
        if (EventUtils.isEventThread() && !isDone()) {
            throw new IllegalStateException(
                    "Can not call get() from the EventThread!");
        }
    }
    
    protected void fireOperationComplete() {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (AsyncFutureListener<V> l : listeners) {
                    l.operationComplete(AsyncFutureTask.this);
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
}
