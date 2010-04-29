package com.ardverk.dht;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.IdentityHashSet;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.concurrent.ExecutorUtils;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkFutureTask;

class RequestManager implements Closeable {
    
    private static final AsyncProcessExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("RequestManagerThread");
    
    private boolean open = true;
    
    private final Set<AsyncFuture<?>> futures 
        = new IdentityHashSet<AsyncFuture<?>>();
    
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
    }
    
    public synchronized <T> ArdverkFuture<T> submit(AsyncProcess<T> process) {
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncRequestFuture<T> future 
            = new AsyncRequestFuture<T>(process);
        
        EXECUTOR.execute(future);
        futures.add(future);
        
        return future;
    }
    
    public synchronized <T> ArdverkFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        AsyncRequestFuture<T> future 
            = new AsyncRequestFuture<T>(process, timeout, unit);
        
        EXECUTOR.execute(future);
        futures.add(future);
        
        return future;
    }
    
    private synchronized void complete(AsyncFuture<?> future) {
        futures.remove(future);
    }
    
    private class AsyncRequestFuture<T> extends ArdverkFutureTask<T> {
        
        public AsyncRequestFuture(AsyncProcess<T> process) {
            super(process);
        }
        
        public AsyncRequestFuture(AsyncProcess<T> process, 
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
