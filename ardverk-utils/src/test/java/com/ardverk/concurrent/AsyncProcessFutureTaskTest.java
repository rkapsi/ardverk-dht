package com.ardverk.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncRunnableFuture;
import org.ardverk.utils.ExceptionUtils;
import org.junit.Test;

public class AsyncProcessFutureTaskTest {

    private static AsyncProcess<String> NOP = new AsyncProcess<String>() {
        @Override
        public void start(AsyncProcessFuture<String> future) {
            future.setValue("Hello World!");
        }
    };
    
    @Test
    public void addListener() 
            throws InterruptedException, ExecutionException {
        AsyncProcessExecutorService executor = AsyncProcessExecutors.newSingleThreadExecutor(
                10L, TimeUnit.SECONDS);

        final CountDownLatch latch = new CountDownLatch(1);
        AsyncFuture<String> future = executor.submit(NOP);
        future.addAsyncFutureListener(new AsyncFutureListener<String>() {
            @Override
            public void operationComplete(AsyncFuture<String> future) {
                latch.countDown();
            }
        });
        
        TestCase.assertTrue(latch.await(1L, TimeUnit.SECONDS));
        
        String value = future.get();
        TestCase.assertTrue(future.isDone());
        TestCase.assertEquals("Hello World!", value);
    }
    
    @Test
    public void addListenerBeforeCompletion() 
            throws InterruptedException, ExecutionException {
        AsyncProcessExecutorService executor = AsyncProcessExecutors.newSingleThreadExecutor(
                10L, TimeUnit.SECONDS);

        final CountDownLatch latch = new CountDownLatch(1);
        
        final AsyncFutureListener<String> listener 
                = new AsyncFutureListener<String>() {
            @Override
            public void operationComplete(AsyncFuture<String> future) {
                latch.countDown();
            }
        };
        
        AsyncProcess<String> process = new AsyncProcess<String>() {
            @Override
            public void start(AsyncProcessFuture<String> future) {
                // Add the listener *before* we're setting the result value!
                future.addAsyncFutureListener(listener);
                future.setValue("Hello World!");
            }
        };
        
        AsyncFuture<String> future = executor.submit(process);
        
        TestCase.assertTrue(latch.await(1L, TimeUnit.SECONDS));
        
        String value = future.get();
        TestCase.assertTrue(future.isDone());
        TestCase.assertEquals("Hello World!", value);
    }
    
    @Test
    public void addListenerAfterCompletion() 
            throws InterruptedException, ExecutionException {
        AsyncProcessExecutorService executor = AsyncProcessExecutors.newSingleThreadExecutor(
                10L, TimeUnit.SECONDS);

        AsyncFuture<String> future = executor.submit(NOP);
        
        String value = future.get();
        TestCase.assertTrue(future.isDone());
        TestCase.assertEquals("Hello World!", value);
        
        final CountDownLatch latch = new CountDownLatch(1);
        future.addAsyncFutureListener(new AsyncFutureListener<String>() {
            @Override
            public void operationComplete(AsyncFuture<String> future) {
                latch.countDown();
            }
        });
        
        TestCase.assertTrue(latch.await(1L, TimeUnit.SECONDS));
    }
    
    @Test
    public void execute() throws InterruptedException, 
            ExecutionException, TimeoutException {
        AsyncProcessExecutorService executor = AsyncProcessExecutors.newSingleThreadExecutor(
                10L, TimeUnit.SECONDS);
        
        AsyncProcess<String> process = new AsyncTask();
        AsyncFuture<String> future = executor.submit(process);
        
        String value = future.get(1L, TimeUnit.SECONDS);
        
        TestCase.assertEquals("Hello World", value);
    }
    
    @Test
    public void timeout() throws InterruptedException, TimeoutException {
        AsyncProcessExecutorService executor = AsyncProcessExecutors.newSingleThreadExecutor(
                250L, TimeUnit.MILLISECONDS);
        
        AsyncProcess<String> process = new AsyncProcess<String>() {
            @Override
            public void start(AsyncProcessFuture<String> future) throws Exception {
                // DO NOTHING
            }
        };
        
        AsyncFuture<String> future = executor.submit(process);
        try {
            future.get(1L, TimeUnit.SECONDS);
            TestCase.fail("Should have failed!");
        } catch (ExecutionException expected) {
            TestCase.assertTrue(ExceptionUtils.isCausedBy(
                    expected, TimeoutException.class));
        } catch (TimeoutException expected) {
            
        }
    }
    
    @Test
    public void checkDefaultConstructor() 
            throws InterruptedException, TimeoutException {
        Executor executor = Executors.newSingleThreadExecutor();
        
        AsyncRunnableFuture<String> future 
            = new AsyncProcessFutureTask<String>();
        
        executor.execute(future);
        
        try {
            future.get(1L, TimeUnit.SECONDS);
            TestCase.fail("Should have failed!");
        } catch (ExecutionException expected) {
            TestCase.assertTrue(ExceptionUtils.isCausedBy(
                    expected, IllegalStateException.class));
        }
    }
    
    private static class AsyncTask implements AsyncProcess<String> {

        private final Executor executor = Executors.newSingleThreadExecutor();
        
        @Override
        public void start(final AsyncProcessFuture<String> future) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    future.setValue("Hello World");
                }
            };
            
            executor.execute(task);
        }
        
    }
}
