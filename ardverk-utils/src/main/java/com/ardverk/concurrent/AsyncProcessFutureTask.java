package com.ardverk.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncFutureTask;
import org.ardverk.concurrent.ExecutorUtils;

import com.ardverk.concurrent.AsyncProcess.Delay;
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
    protected void doRun() throws Exception {
        if (!isDone()) {
            watchdog(timeout, unit);
            start();
        }
    }
    
    /**
     * Starts the {@link AsyncProcess}. You may override this method for
     * custom implementations.
     */
    protected void start() throws Exception {
        process.start(this);
    }
    
    /**
     * Starts the watchdog task and returns true on success.
     */
    private synchronized boolean watchdog(long timeout, TimeUnit unit) {
        if (timeout < 0L) {
            return false;
        }
        
        if (isDone()) {
            return false;
        }
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (AsyncProcessFutureTask.this) {
                    if (!isDone() && !isDelay()) {
                        wasTimeout = true;
                        handleTimeout();
                    }
                }
            }
        };
        
        future = EXECUTOR.schedule(task, timeout, unit);
        return true;
    }
    
    /**
     * Returns true if the watchdog should be delayed.
     */
    private boolean isDelay() {
        long delay = getDelay(unit);
        return watchdog(delay, unit);
    }
    
    /**
     * You may override this method for custom delay implementations.
     */
    protected long getDelay(TimeUnit unit) {
        if (process instanceof Delay) {
            return ((Delay)process).getDelay(unit);
        }
        return -1L;
    }
    
    /**
     * Called by the watchdog when a timeout occurred. The default
     * implementation will simply call {@link #setException(Throwable)}
     * with a {@link TimeoutException}.
     */
    protected synchronized void handleTimeout() {
        setException(new TimeoutException());
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
