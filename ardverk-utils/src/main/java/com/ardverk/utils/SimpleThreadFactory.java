package com.ardverk.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {
    
    private final AtomicInteger instance = new AtomicInteger();
    
    private final String name;

    public SimpleThreadFactory(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + "-" + instance.getAndIncrement());
    }
}
