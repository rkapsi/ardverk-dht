package com.ardverk.dht.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.ExecutorUtils;

public class SchedulingUtils {

    private static final ScheduledThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor("DefaultSchedulingThread");
    
    private SchedulingUtils() {}
    
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, 
            long initialDelay, long delay, TimeUnit unit) {
        return EXECUTOR.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
}
