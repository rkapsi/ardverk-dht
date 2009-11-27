package com.ardverk.dht;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncExecutors;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;

class RequestManager implements Closeable {

    private static final AsyncExecutorService EXECUTOR 
        = AsyncExecutors.newCachedThreadPool(
                AsyncExecutors.defaultThreadFactory(
                        "RequestManagerThread"));
    
    static {
        
    }
    
    private int counter = 0;
    
    private boolean open = true;
    
    private final Map<Integer, AsyncFuture<?>> futures 
        = new HashMap<Integer, AsyncFuture<?>>();
    
    @Override
    public synchronized void close() {
        if (!open) {
            return;
        }
        
        open = false;
        
        for (AsyncFuture<?> future : futures.values()) {
            future.cancel(true);
        }
        
        futures.clear();
    }
    
    public synchronized <T> AsyncFuture<T> submit(AsyncProcess<T> process) {
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncFuture<T> future = EXECUTOR.submit(process);
        add(future);
        return future;
    }
    
    public synchronized <T> AsyncFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncFuture<T> future = EXECUTOR.submit(process);
        add(future);
        return future;
    }
    
    private synchronized <T> Integer add(AsyncFuture<T> future) {
        final Integer key = Integer.valueOf(counter++);
        
        future.addAsyncFutureListener(new AsyncFutureListener<T>() {
            @Override
            public void operationComplete(AsyncFuture<T> future) {
                synchronized (RequestManager.this) {
                    futures.remove(key);
                }
            }
        });
        
        AsyncFuture<?> existing = futures.put(key, future);
        assert (existing == null);
        
        return key;
    }
}
