package com.ardverk.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;

public abstract class SafeRunnable implements Runnable {

    @Override
    public void run() {
        try {
            innerRun();
        } catch (Exception err) {
            uncaughtException(err);
        }
    }
    
    protected abstract void innerRun();
    
    protected void uncaughtException(Throwable t) {
        Thread thread = Thread.currentThread();
        UncaughtExceptionHandler ueh 
            = thread.getUncaughtExceptionHandler();
        ueh.uncaughtException(thread, t);
    }
}
