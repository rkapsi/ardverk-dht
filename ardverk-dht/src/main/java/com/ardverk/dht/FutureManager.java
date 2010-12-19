/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.IdentityHashSet;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.concurrent.ExecutorUtils;
import org.ardverk.concurrent.FutureUtils;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkFutureTask;
import com.ardverk.dht.concurrent.ArdverkProcess;

/**
 * The {@link FutureManager} manages {@link ArdverkFuture}s.
 */
public class FutureManager implements Closeable {
    
    private static final AsyncProcessExecutorService CACHED_THREAD_EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("FutureManagerCachedThread");
    
    private static final AsyncProcessExecutorService SINGLE_THREAD_EXECUTOR
        = ExecutorUtils.newSingleThreadExecutor("FutureManagerSingleThread");
    
    private final Set<AsyncFuture<?>> futures 
        = new IdentityHashSet<AsyncFuture<?>>();
    
    private boolean open = true;
    
    @Override
    public synchronized void close() {
        if (!open) {
            return;
        }
        
        open = false;
        
        FutureUtils.cancelAll(futures, true);
        futures.clear();
    }
    
    /**
     * Submits the given {@link ArdverkProcess} for execution and returns
     * an {@link ArdverkFuture} for it.
     */
    public synchronized <T> ArdverkFuture<T> submit(ExecutorKey executorKey, 
            ArdverkProcess<T> process, long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFuture<T> future 
            = new ManagedFuture<T>(process, timeout, unit);
        
        getExecutor(executorKey).execute(future);
        futures.add(future);
        
        return future;
    }
    
    /**
     * Returns an {@link Executor} for the given {@link ExecutorKey}.
     */
    private static Executor getExecutor(ExecutorKey executorKey) {
        switch (executorKey) {
            case PARALLEL:
                return CACHED_THREAD_EXECUTOR;
            default:
                return SINGLE_THREAD_EXECUTOR;
        }
    }
    
    /**
     * Callback for completed {@link ArdverkFuture}s.
     */
    private synchronized void complete(ArdverkFuture<?> future) {
        futures.remove(future);
    }
    
    private class ManagedFuture<T> extends ArdverkFutureTask<T> {
        
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