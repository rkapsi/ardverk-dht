/*
 * Copyright 2009 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ardverk.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutor;
import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncFuture;

/**
 * An {@link AsyncExecutor} that provides methods to manage termination and
 * methods that can produce an {@link AsyncFuture} for tracking progress of
 * one or more asynchronous tasks.
 * 
 * @see AsyncExecutor
 * @see ExecutorService
 */
public interface AsyncProcessExecutorService extends AsyncProcessExecutor, AsyncExecutorService {

    /**
     * Sets default timeout for {@link AsyncFuture}s. Use -1 to disable 
     * the default timeout.
     */
    public void setTimeout(long timeout, TimeUnit unit);
    
    /**
     * Returns the default timeout of {@link AsyncFuture}s or -1 if no
     * timeout is specified (default).
     */
    public long getTimeout(TimeUnit unit);
    
    /**
     * 
     */
    public long getTimeoutInMillis();
    
    /**
     * Submits the given {@link AsyncProcess} for execution and returns 
     * an {@link AsyncFuture} for it.
     */
    public <T> AsyncProcessFuture<T> submit(AsyncProcess<T> process);
    
    /**
     * Submits the given {@link AsyncProcess} for execution and returns 
     * an {@link AsyncFuture} for it.
     */
    public <T> AsyncProcessFuture<T> submit(AsyncProcess<T> process, 
            long timeout, TimeUnit unit);
    
    
}
