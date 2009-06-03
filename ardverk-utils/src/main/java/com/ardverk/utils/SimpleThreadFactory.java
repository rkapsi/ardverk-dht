package com.ardverk.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {
    
    private final AtomicInteger instance = new AtomicInteger();
    
    private final String name;

    private final int priority;
    
    private final boolean daemon;
    
    private final ClassLoader classLoader;
    
    private final UncaughtExceptionHandler ueh;
    
    public SimpleThreadFactory(String name) {
        this(name, Thread.NORM_PRIORITY, false, null, null);
    }
    
    public SimpleThreadFactory(String name, int priority) {
        this(name, priority, false, null, null);
    }
    
    public SimpleThreadFactory(String name, boolean daemon) {
        this(name, Thread.NORM_PRIORITY, daemon, null, null);
    }
    
    public SimpleThreadFactory(String name, UncaughtExceptionHandler ueh) {
        this(name, Thread.NORM_PRIORITY, false, null, ueh);
    }
    
    public SimpleThreadFactory(String name, int priority, 
            boolean daemon, ClassLoader classLoader, 
            UncaughtExceptionHandler ueh) {
        
        if (name == null) {
            throw new NullPointerException("name");
        }
        
        if (priority < Thread.MIN_PRIORITY 
                || Thread.MAX_PRIORITY < priority) {
            throw new IllegalArgumentException("priority=" + priority);
        }
        
        this.name = name;
        this.priority = priority;
        this.daemon = daemon;
        this.classLoader = classLoader;
        this.ueh = ueh;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, name + "-" + instance.getAndIncrement());
        thread.setPriority(priority);
        thread.setDaemon(daemon);
        
        if (classLoader != null) {
            thread.setContextClassLoader(classLoader);
        }
        
        if (ueh != null) {
            thread.setUncaughtExceptionHandler(ueh);
        }
        
        return thread;
    }
}
