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

public class FutureManager implements Closeable {
    
    private static final AsyncProcessExecutorService EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("FutureManagerThread");
    
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
        
        ManagedFuture<T> future 
            = new ManagedFuture<T>(process);
        
        EXECUTOR.execute(future);
        futures.add(future);
        
        return future;
    }
    
    public synchronized <T> ArdverkFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFuture<T> future 
            = new ManagedFuture<T>(process, timeout, unit);
        
        EXECUTOR.execute(future);
        futures.add(future);
        
        return future;
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
