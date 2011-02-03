/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht;

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
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTFutureTask;
import org.ardverk.dht.concurrent.DHTProcess;


/**
 * The {@link FutureManager} manages {@link DHTFuture}s.
 */
public class FutureManager implements Closeable {
    
    private static final AsyncProcessExecutorService CACHED_THREAD_EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("FutureManagerCachedThread");
    
    private static final AsyncProcessExecutorService SINGLE_THREAD_EXECUTOR
        = ExecutorUtils.newSingleThreadExecutor("FutureManagerSingleThread");
    
    /**
     * The {@link Key} controls how a particular operation should
     * be executed.
     */
    public static enum Key {
        
        /**
         * The {@link #SERIAL} {@link ExecutorKey} executions enqueued operations
         * in a serial fashion.
         */
        SERIAL(SINGLE_THREAD_EXECUTOR),
        
        /**
         * The {@link #PARALLEL} {@link ExecutorKey} executions enqueued operations
         * in a parallel fashion.
         */
        PARALLEL(CACHED_THREAD_EXECUTOR);
        
        /**
         * The default {@link Key} that should be used unless there
         * is a reason not to use this {@link Key}.
         */
        public static final Key DEFAULT = Key.PARALLEL;
        
        /**
         * The {@link Key} that should be used for backend and possibly 
         * for other low priority operations.
         */
        public static final Key BACKEND = Key.SERIAL;
        
        private final Executor executor;
        
        private Key(Executor executor) {
            this.executor = executor;
        }

        /**
         * Executes the given {@link Runnable}.
         */
        private void execute(Runnable command) {
            executor.execute(command);
        }
    }
    
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
     * Submits the given {@link DHTProcess} for execution and returns
     * an {@link DHTFuture} for it.
     */
    public synchronized <T> DHTFuture<T> submit(Key executorKey, 
            DHTProcess<T> process, long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFutureTask<T> future 
            = new ManagedFutureTask<T>(process, timeout, unit);
        
        executorKey.execute(future);
        futures.add(future);
        
        return future;
    }
    
    /**
     * Callback for completed {@link DHTFuture}s.
     */
    private synchronized void complete(DHTFuture<?> future) {
        futures.remove(future);
    }
    
    private class ManagedFutureTask<T> extends DHTFutureTask<T> {
        
        public ManagedFutureTask(AsyncProcess<T> process, 
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