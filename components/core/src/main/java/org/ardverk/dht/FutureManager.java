/*
 * Copyright 2009-2012 Roger Kapsi
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
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.ardverk.collection.IdentityHashSet;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.dht.concurrent.DHTExecutor;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTFutureTask;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.ExecutorKey;
import org.ardverk.dht.config.Config;


/**
 * The {@link FutureManager} manages {@link DHTFuture}s.
 */
@Singleton
public class FutureManager implements Closeable {
    
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
    
    public <V> DHTFuture<V> submit(DHTProcess<V> process, Config config) {
        ExecutorKey executorKey = config.getExecutorKey();
        long timeout = config.getOperationTimeoutInMillis();
        return submit(executorKey, process, timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Submits the given {@link DHTProcess} for execution and returns
     * an {@link DHTFuture} for it.
     */
    public synchronized <T> DHTFuture<T> submit(ExecutorKey executorKey, 
            DHTProcess<T> process, long timeout, TimeUnit unit) {
        
        if (!open) {
            throw new IllegalStateException();
        }
        
        ManagedFutureTask<T> future 
            = new ManagedFutureTask<T>(process, timeout, unit);
        
        DHTExecutor.execute(executorKey, future);
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