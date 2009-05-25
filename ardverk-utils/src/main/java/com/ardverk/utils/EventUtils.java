package com.ardverk.utils;

import java.util.concurrent.ThreadPoolExecutor;

public class EventUtils {

    private static final ThreadPoolExecutor EXECUTOR 
        = ExecutorUtils.newSingleThreadExecutor("EventUtilsThread");

    private EventUtils() {
    }

    public static void fireEvent(Runnable event) {
        EXECUTOR.execute(event);
    }
}
