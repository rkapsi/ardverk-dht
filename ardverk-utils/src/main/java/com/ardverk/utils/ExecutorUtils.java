package com.ardverk.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ardverk.concurrent.SafeRunnable;

public class ExecutorUtils {

    private static final long PURGE = 30L*1000L;
    
    private ExecutorUtils() {
    }

    public static ThreadPoolExecutor newSingleThreadExecutor(String name) {
        return (ThreadPoolExecutor) Executors.newSingleThreadExecutor(
                new SimpleThreadFactory(name));
    }
    
    public static ScheduledThreadPoolExecutor newScheduledThreadPool(int count, String name) {

        final ScheduledThreadPoolExecutor executor 
            = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(count, 
                        new SimpleThreadFactory(name));
        
        if (PURGE > 0L) {
            Runnable task = new SafeRunnable() {
                @Override
                protected void innerRun() {
                    executor.purge();
                }
            };
            
            executor.scheduleWithFixedDelay(task, PURGE, 
                    PURGE, TimeUnit.MILLISECONDS);
        }
        
        return executor;
    }
}
