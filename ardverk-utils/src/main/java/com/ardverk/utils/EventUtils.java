package com.ardverk.utils;

import org.ardverk.concurrent.AsyncExecutors;

public class EventUtils {
    
    private EventUtils() {
    }

    public static boolean isEventThread() {
        return AsyncExecutors.isEventThread();
    }
    
    public static void fireEvent(Runnable event) {
        AsyncExecutors.fireEvent(event);
    }
}
