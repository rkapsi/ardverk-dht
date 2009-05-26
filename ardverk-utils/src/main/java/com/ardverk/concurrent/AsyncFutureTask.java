package com.ardverk.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ardverk.utils.EventUtils;
import com.ardverk.utils.ExecutorUtils;

public class AsyncFutureTask<V> implements Runnable, AsyncFuture<V> {
    
    private static final ScheduledThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newScheduledThreadPool(4, "AsyncFutureTaskThread");
    
    private final List<AsyncFutureListener<V>> listeners 
        = new ArrayList<AsyncFutureListener<V>>();
    
    private final OnewayExchanger<V> exchanger 
        = new OnewayExchanger<V>(this, true);
    
    private final AsyncProcess<V> process;
    
    private final long timeout;
    
    private final TimeUnit unit;
    
    private boolean running = false;
    
    private ScheduledFuture<?> watchdog = null;
    
    private boolean wasTimeout = false;
    
    public AsyncFutureTask(AsyncProcess<V> process, long timeout, TimeUnit unit) {
        if (process == null) {
            throw new NullPointerException("processs");
        }
        
        if (timeout < 0L) {
            throw new IllegalArgumentException("timeout=" + timeout);
        }
        
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        
        this.process = process;
        this.timeout = timeout;
        this.unit = unit;
    }
    
    @Override
    public synchronized void run() {
        if (running) {
            throw new IllegalStateException("Already running!");
        }
        
        try {
            if (!isDone()) {
                process.start(this);
                initWatchdog();
                running = true;
            }
        } catch (Throwable t) {
            setException(t);
        }
    }
    
    private void initWatchdog() {
        if (timeout == 0L) {
            return;
        }
        
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
        
        watchdog = EXECUTOR.schedule(task, timeout, unit);
    }
    
    protected void watchdogWillKillFuture() {
        // OVERRIDE
    }
    
    @Override
    public void setValue(V value) {
        boolean callDone = false;
        synchronized (this) {
            if (!exchanger.isDone()) {
                exchanger.setValue(value);
                callDone = true;
            }
            
            if (watchdog != null) {
                watchdog.cancel(true);
                watchdog = null;
            }
        }
        
        if (callDone) {
            innerDone();
        }
    }
    
    @Override
    public void setException(Throwable t) {
        if (t == null) {
            throw new NullPointerException("t");
        }
        
        boolean callDone = false;
        synchronized (this) {
            if (!exchanger.isDone()) {
                exchanger.setException(new ExecutionException(t));
                callDone = true;
            }
            
            if (watchdog != null) {
                watchdog.cancel(true);
                watchdog = null;
            }
        }
        
        if (callDone) {
            innerDone();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = false;
        boolean callDone = false;
        synchronized (this) {
            cancelled = exchanger.isDone();
            if (!cancelled) {
                cancelled = exchanger.cancel();
                callDone = true;
            }
        }
        
        if (callDone) {
            innerDone();
        }
        
        return cancelled;
    }

    @Override
    public synchronized boolean isTimeout() {
        return isDone() && wasTimeout;
    }
    
    @Override
    public V get() throws InterruptedException, ExecutionException {
        checkIfEventThread();
        return exchanger.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        checkIfEventThread();
        return exchanger.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        return exchanger.isCancelled();
    }

    @Override
    public boolean isDone() {
        return exchanger.isDone();
    }
    
    @Override
    public boolean throwsException() {
        return exchanger.throwsException();
    }

    @Override
    public V tryGet() throws InterruptedException, ExecutionException {
        return exchanger.tryGet();
    }

    private void innerDone() {
        done();
        fireOperationComplete();
    }
    
    protected void done() {
        // OVERRIDE
    }
    
    @Override
    public synchronized void addAsyncFutureListener(final AsyncFutureListener<V> l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        if (isDone()) {
            Runnable event = new Runnable() {
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
    public synchronized void removeAsyncFutureListener(AsyncFutureListener<V> l) {
        if (l == null) {
            throw new NullPointerException("l");
        }
        
        listeners.remove(l);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized AsyncFutureListener<V>[] getAsyncFutureListeners() {
        return listeners.toArray(new AsyncFutureListener[0]);
    }
    
    protected void fireOperationComplete() {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                for (AsyncFutureListener<V> l : getAsyncFutureListeners()) {
                    l.operationComplete(AsyncFutureTask.this);
                }
            }
        };
        
        EventUtils.fireEvent(event);
    }
    
    /**
     * Make sure can not call {@link #get()} and {@link #get(long, TimeUnit)}
     * from the {@link EventUtils} Thread as it is very difficult to debug.
     */
    protected void checkIfEventThread() throws IllegalStateException {
        if (EventUtils.isEventThread() && !isDone()) {
            throw new IllegalStateException(
                    "Can not call get() from the EventThread!");
        }
    }
}
