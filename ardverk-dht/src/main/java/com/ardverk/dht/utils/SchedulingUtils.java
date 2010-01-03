package com.ardverk.dht.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ardverk.utils.ExecutorUtils;

public class SchedulingUtils {

    private static final ScheduledThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newSingleThreadScheduledExecutor("");
    
    private SchedulingUtils() {}
    
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, 
            long initialDelay, long delay, TimeUnit unit) {
        return EXECUTOR.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
}
