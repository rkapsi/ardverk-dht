/*
 * Copyright 2010 Roger Kapsi
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

import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutor;

import com.ardverk.utils.EventThreadProvider;

/**
 * Factory and utility methods for {@link AsyncExecutor}, 
 * {@link AsyncProcessExecutorService}, {@link ThreadFactory}, 
 * {@link AsyncProcess}, {@link Runnable} and {@link Callable} 
 * classes defined in this package.
 * 
 * This class is also responsible for event dispatching through
 * the {@link EventThreadProvider} interface. Custom implementations 
 * may be loaded through the {@link ServiceLoader} facilities.
 * 
 * @see Executors
 * @see ServiceLoader
 * @see EventThreadProvider
 */
public class AsyncProcessExecutors {
    
    private AsyncProcessExecutors() {}
    
    /**
     * Creates and returns a cached {@link Thread} pool with no timeout.
     * 
     * @see Executors#newCachedThreadPool()
     */
    public static AsyncProcessExecutorService newCachedThreadPool() {
        return newCachedThreadPool(-1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a cached {@link Thread} pool with the given
     * timeout.
     * 
     * @see Executors#newCachedThreadPool()
     */
    public static AsyncProcessExecutorService newCachedThreadPool(
            long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
    
    /**
     * Creates and returns a cached {@link Thread} pool with no timeout.
     * 
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static AsyncProcessExecutorService newCachedThreadPool(
            ThreadFactory threadFactory) {
        return newCachedThreadPool(threadFactory, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a cached {@link Thread} pool with the given
     * timeout.
     * 
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static AsyncProcessExecutorService newCachedThreadPool(
            ThreadFactory threadFactory, long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory,
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
    
    /**
     * Creates and returns a single {@link Thread} executor with no timeout.
     * 
     * @see Executors#newSingleThreadExecutor()
     */
    public static AsyncProcessExecutorService newSingleThreadExecutor() {
        return newSingleThreadExecutor(-1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a single {@link Thread} executor with the given
     * timeout.
     * 
     * @see Executors#newSingleThreadExecutor()
     */
    public static AsyncProcessExecutorService newSingleThreadExecutor(
            long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
    
    /**
     * Creates and returns a single {@link Thread} executor with no timeout.
     * 
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static AsyncProcessExecutorService newSingleThreadExecutor(
            ThreadFactory threadFactory) {
        return newSingleThreadExecutor(threadFactory, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a single {@link Thread} executor with the given
     * timeout.
     * 
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static AsyncProcessExecutorService newSingleThreadExecutor(
            ThreadFactory threadFactory, long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory,
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
    
    /**
     * Creates and returns a fixed size {@link Thread} pool with no timeout.
     * 
     * @see Executors#newFixedThreadPool(int)
     */
    public static AsyncProcessExecutorService newFixedThreadPool(int nThreads) {
        return newFixedThreadPool(nThreads, -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a fixed size {@link Thread} executor with the given
     * timeout.
     * 
     * @see Executors#newFixedThreadPool(int)
     */
    public static AsyncProcessExecutorService newFixedThreadPool(int nThreads, 
            long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
    
    /**
     * Creates and returns a fixed size {@link Thread} pool with no timeout.
     * 
     * @see Executors#newFixedThreadPool(int, ThreadFactory)
     */
    public static AsyncProcessExecutorService newFixedThreadPool(
            int nThreads, ThreadFactory threadFactory) {
        return newFixedThreadPool(nThreads, threadFactory, 
                -1L, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates and returns a fixed size {@link Thread} executor with the given
     * timeout.
     * 
     * @see Executors#newFixedThreadPool(int, ThreadFactory)
     */
    public static AsyncProcessExecutorService newFixedThreadPool(int nThreads, 
            ThreadFactory threadFactory, long timeout, TimeUnit unit) {
        AsyncProcessThreadPoolExecutor executor 
            = new AsyncProcessThreadPoolExecutor(
                nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), 
                threadFactory,
                -1L, TimeUnit.MILLISECONDS);
        executor.setTimeout(timeout, unit);
        return executor;
    }
}
