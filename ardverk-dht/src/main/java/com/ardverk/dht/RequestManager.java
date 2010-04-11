package com.ardverk.dht;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.ExecutorUtils;

import com.ardverk.concurrent.AsyncProcess;
import com.ardverk.concurrent.AsyncProcessExecutorService;
import com.ardverk.concurrent.AsyncProcessExecutors;
import com.ardverk.concurrent.AsyncProcessFuture;
import com.ardverk.concurrent.AsyncProcessFutureTask;

class RequestManager implements Closeable {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private static final AsyncProcessExecutorService EXECUTOR 
        = AsyncProcessExecutors.newCachedThreadPool(
                ExecutorUtils.defaultThreadFactory("RequestManagerThread"));
    
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
    
    public synchronized <T> AsyncProcessFuture<T> submit(AsyncProcess<T> process) {
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncRequestFuture<T> future 
            = new AsyncRequestFuture<T>(process);
        
        EXECUTOR.execute(future);
        futures.put(future.key, future);
        
        return future;
    }
    
    public synchronized <T> AsyncProcessFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncRequestFuture<T> future 
            = new AsyncRequestFuture<T>(process, timeout, unit);
        
        EXECUTOR.execute(future);
        futures.put(future.key, future);
        
        return future;
    }
    
    private class AsyncRequestFuture<T> extends AsyncProcessFutureTask<T> {
        
        private final Integer key = Integer.valueOf(COUNTER.incrementAndGet());
        
        public AsyncRequestFuture(AsyncProcess<T> process) {
            super(process);
        }
        
        public AsyncRequestFuture(AsyncProcess<T> process, 
                long timeout, TimeUnit unit) {
            super(process, timeout, unit);
        }

        @Override
        protected void done() {
            synchronized (RequestManager.this) {
                futures.remove(key);
            }
            
            super.done();
        }
    }
}
