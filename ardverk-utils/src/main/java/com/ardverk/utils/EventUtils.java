package com.ardverk.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class EventUtils {

    private static AtomicReference<Thread> REFERENCE 
        = new AtomicReference<Thread>();
    
    private static final ThreadFactory FACTORY
            = new SimpleThreadFactory("EventUtilsThread") {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = super.newThread(r);
            REFERENCE.set(thread);
            return thread;
        }
    };
    
    private static final Executor EXECUTOR 
        = Executors.newSingleThreadExecutor(FACTORY);

    private EventUtils() {
    }

    public static boolean isEventThread() {
        return REFERENCE.get() == Thread.currentThread();
    }
    
    public static void fireEvent(Runnable event) {
        EXECUTOR.execute(event);
    }
}
