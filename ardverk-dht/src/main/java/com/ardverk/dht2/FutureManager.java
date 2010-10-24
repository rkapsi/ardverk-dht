package com.ardverk.dht2;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.IdentityHashSet;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureGroup;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.concurrent.ExecutorGroup;
import org.ardverk.concurrent.ExecutorQueue;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.concurrent.Scheduler;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkFutureTask;

/**
 * The {@link FutureManager} manages {@link AsyncFuture}s.
 */
public class FutureManager implements Closeable {
    
    private static final AsyncProcessExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("FutureManagerThread");
    
    private static final int DEFAULT_CONCURRENCY_LEVEL = 4;
    
    private final Map<QueueKey, ExecutorQueue<? extends Runnable>> executors 
        = new EnumMap<QueueKey, ExecutorQueue<? extends Runnable>>(QueueKey.class);
    
    private final Set<AsyncFuture<?>> futures 
        = new IdentityHashSet<AsyncFuture<?>>();
    
    private boolean open = true;
    
    public FutureManager() {
        this (DEFAULT_CONCURRENCY_LEVEL);
    }
    
    public FutureManager(int concurrencyLevel) {
        executors.put(QueueKey.PARALLEL, new ExecutorGroup(EXECUTOR));
        executors.put(QueueKey.SERIAL, new AsyncFutureGroup(EXECUTOR, concurrencyLevel));
    }
    
    public ExecutorGroup createExecutorGroup() {
        return new ExecutorGroup(EXECUTOR);
    }
    
    public ExecutorGroup createExecutorGroup(Scheduler scheduler) {
        return new ExecutorGroup(EXECUTOR, scheduler);
    }
    
    public AsyncFutureGroup createAsyncFutureGroup(int concurrencyLevel) {
        return new AsyncFutureGroup(EXECUTOR, concurrencyLevel);
    }
    
    @Override
    public synchronized void close() {
        if (!open) {
            return;
        }
        
        open = false;
        
        for (AsyncFuture<?> future : futures) {
            future.cancel(true);
        }
        
        futures.clear();
        
        for (ExecutorQueue<?> queue : executors.values()) {
            queue.shutdown();
        }
    }
    
    public synchronized <T> ArdverkFuture<T> submit(QueueKey queueKey, 
            AsyncProcess<T> process) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFuture<T> future 
            = new ManagedFuture<T>(process);
        
        getQueue(queueKey).execute(future);
        futures.add(future);
        
        return future;
    }
    
    public synchronized <T> ArdverkFuture<T> submit(QueueKey queueKey, 
            AsyncProcess<T> process, long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFuture<T> future 
            = new ManagedFuture<T>(process, timeout, unit);
        
        getQueue(queueKey).execute(future);
        futures.add(future);
        
        return future;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutorQueue<Runnable> getQueue(QueueKey queueKey) {
        return (ExecutorQueue<Runnable>)executors.get(queueKey);
    }
    
    private synchronized void complete(AsyncFuture<?> future) {
        futures.remove(future);
    }
    
    private class ManagedFuture<T> extends ArdverkFutureTask<T> {
        
        public ManagedFuture(AsyncProcess<T> process) {
            super(process);
        }
        
        public ManagedFuture(AsyncProcess<T> process, 
                long timeout, TimeUnit unit) {
            super(process, timeout, unit);
        }

        @Override
        protected void done0() {
            complete(this);
            super.done0();
        }
    }
}
