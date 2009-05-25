package com.ardverk.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtils {

    private ExecutorUtils() {
    }

    public static ThreadPoolExecutor newSingleThreadExecutor(final String name) {
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        };

        return (ThreadPoolExecutor) Executors.newSingleThreadExecutor(factory);
    }
}
