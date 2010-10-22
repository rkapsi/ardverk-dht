package com.ardverk.dht.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.lang.Arguments;

/**
 * 
 */
public class ArdverkFutureQueue<V> {

    private final AsyncFutureListener<V> listener 
            = new AsyncFutureListener<V>() {
        @Override
        public void operationComplete(AsyncFuture<V> future) {
            processNext(true);
        }
    };
    
    private final Executor executor;

    private final Queue<ArdverkRunnableFuture<V>> queue;
    
    private final int concurrencyLevel;
    
    private int active = 0;
    
    public ArdverkFutureQueue(Executor executor, int concurrencyLevel) {
        this(executor, new LinkedList<ArdverkRunnableFuture<V>>(), 
                concurrencyLevel);
    }
    
    public ArdverkFutureQueue(Executor executor, 
            Queue<ArdverkRunnableFuture<V>> queue, int concurrencyLevel) {
        this.executor = Arguments.notNull(executor, "executor");
        this.queue = Arguments.notNull(queue, "queue");
        this.concurrencyLevel = Arguments.greaterZero(
                concurrencyLevel, "concurrencyLevel");
    }
    
    public synchronized int size() {
        return queue.size();
    }
    
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
    
    public void clear() {
        clear(true);
    }
    
    public void clear(boolean mayInterruptIfRunning) {
        List<Future<?>> copy = null;
        synchronized (this) {
            copy = new ArrayList<Future<?>>(queue);
            queue.clear();
        }
        
        for (Future<?> future : copy) {
            future.cancel(mayInterruptIfRunning);
        }
    }
    
    public ArdverkFuture<V> submit(AsyncProcess<V> process, 
            long timeout, TimeUnit unit) {
        
        ArdverkFutureTask<V> future = newFutureTask(process, timeout, unit);
        
        synchronized (this) {
            boolean success = queue.offer(future);
            if (success) {
                processNext(false);
            }
        }
        
        return future;
    }
    
    protected ArdverkFutureTask<V> newFutureTask(AsyncProcess<V> process, 
            long timeout, TimeUnit unit) {
        return new ArdverkFutureTask<V>(process, timeout, unit);
    }
    
    private synchronized boolean processNext(boolean callback) {
        if (callback) {
            --active;
        }
        
        if (active < concurrencyLevel && !queue.isEmpty()) {
            ArdverkRunnableFuture<V> task = queue.poll();
            task.addAsyncFutureListener(listener);
            executor.execute(task);
            ++active;
            return true;
        }
        return false;
    }
}
