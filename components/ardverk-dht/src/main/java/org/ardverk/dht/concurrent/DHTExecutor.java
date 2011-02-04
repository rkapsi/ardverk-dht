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

package org.ardverk.dht.concurrent;

import java.util.concurrent.Executor;

import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.concurrent.ExecutorUtils;

/**
 * The {@link DHTExecutor} provides {@link Executor}s for the DHT.
 */
public class DHTExecutor {

    // TODO: Use fixed-size Thread Pool with a non-blocking queue?
    private static final AsyncProcessExecutorService CACHED_THREAD_EXECUTOR 
        = ExecutorUtils.newCachedThreadPool("DHTExecutorCachedThread");
    
    private static final AsyncProcessExecutorService SINGLE_THREAD_EXECUTOR
        = ExecutorUtils.newSingleThreadExecutor("DHTExecutorSingleThread");
    
    /**
     * The {@link Key} controls how a particular operation should
     * be executed.
     */
    public static enum Key {
        
        /**
         * The {@link #SERIAL} {@link Key} executions enqueued operations
         * in a serial fashion.
         */
        SERIAL(SINGLE_THREAD_EXECUTOR),
        
        /**
         * The {@link #PARALLEL} {@link Key} executions enqueued operations
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
    
    private DHTExecutor() {}
    
    /**
     * Executes the given {@link Runnable}.
     */
    public static void execute(Key executorKey, Runnable command) {
        executorKey.execute(command);
    }
}
