package com.ardverk.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ardverk.utils.ExceptionUtils;

/**
 * The OnewayExchanger is an one-way synchronization point
 * for Threads. One or more Threads can wait for the arrival
 * of a value by calling the get() method which will block
 * and suspend the Threads until an another Thread sets a
 * return value or an Exception which will be thrown by the
 * get() method.
 * 
 * The main differences between OnewayExchanger and 
 * {@link Exchanger} are:
 * 
 * <ol>
 * <li> Multiple Threads can wait for a result from a single Thread
 * <li> It's an one-way exchange
 * <li> The setter Thread may sets an Exception as the result causing
 * it to be thrown on the getter side
 * <li> The OnewayExchanger is cancellable
 * <li> The OnewayExchanger can be configured for a single shot. That
 * means once an return value or an Exception have been set they cannot
 * be changed anymore.
 * </ol>
 */
public class OnewayExchanger<V, E extends Throwable> {
    
    /** Flag for whether or not we're done */
    private boolean done = false;
    
    /** Flag for whether or not the exchanger was canceled */
    private boolean cancelled = false;
    
    /** Flag for whether or not this is an one-shot exchanger */
    private final boolean oneShot;
    
    /** The value we're going to return */
    private V value;
    
    /** The Exception we're going to throw */
    private E exception;
    
    private final boolean captureCallerStack;
    
    /** 
     * The stack of the Thread which called {@link #setValue(Object)},
     * {@link #setException(Throwable)} or {@link #cancel()}
     */
    private Throwable callerStack;
    
    /**
     * Creates an OnewayExchanger with the default configuration.
     */
    public OnewayExchanger() {
        this(false, false);
    }
    
    /**
     * Creates an OnewayExchanger that is either configured
     * for a single shot which means the return value or the
     * Exception cannot be changed after they've been set.
     * 
     * Default is false.
     */
    public OnewayExchanger(boolean oneShot) {
        this(oneShot, false);
    }
    
    public OnewayExchanger(boolean oneShot, boolean callerStack) {
        this.oneShot = oneShot;
        this.captureCallerStack = callerStack;
    }
    
    /**
     * Waits for another Thread for a value or an Exception
     * unless they're already set in which case this method
     * will return immediately.
     */
    public synchronized V get() throws InterruptedException, E {
        try {
            return get(0L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException cannotHappen) {
            throw new Error(cannotHappen);
        }
    }
    
    /**
     * Waits for another Thread for the given time for a value 
     * or an Exception unless they're already set in which case 
     * this method will return immediately.
     */
    public synchronized V get(long timeout, TimeUnit unit) 
            throws InterruptedException, TimeoutException, E {
        
        if (!done) {
            if (timeout == 0L) {
                wait();
            } else {
                unit.timedWait(this, timeout);
            }
            
            // Not done? Must be a timeout!
            if (!done) {
                throw new TimeoutException();
            }
        }
        
        if (cancelled) {
            throw new CancellationException();
        }
        
        // Prioritize Exceptions!
        if (exception != null) {
            throw exception;
        }
        
        return value;
    }
    
    /**
     * Tries to get the value without blocking.
     */
    public synchronized V tryGet() throws InterruptedException, E {
        if (done) {
            return get();
        } else {
            return null;
        }
    }
    
    /**
     * Tries to cancel the OnewayExchanger and returns true
     * on success.
     */
    public synchronized boolean cancel() {
        if (done) {
            return cancelled;
        }
        
        done = true;
        cancelled = true;
        
        if (captureCallerStack) {
            callerStack = new Exception();
        }
        
        notifyAll();
        return true;
    }
    
    /**
     * Returns true if the OnewayExchanger is canceled
     */
    public synchronized boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Returns true if the get() method will return immediately
     * by throwing an Exception or returning a value
     */
    public synchronized boolean isDone() {
        return done;
    }
    
    /**
     * Returns true if calling the get() method will
     * throw an Exception
     */
    public synchronized boolean throwsException() {
        return cancelled || exception != null;
    }
    
    /**
     * Returns true if this is an one-shot OnewayExchanger
     */
    public boolean isOneShot() {
        return oneShot;
    }
    
    /**
     * Sets the value that will be returned by the get() method
     */
    public synchronized void setValue(V value) {
        if (cancelled) {
            return;
        }
        
        if (done && oneShot) {
            throw new IllegalStateException("The OnewayExchanger is configured for a single shot");
        }
        
        if (captureCallerStack) {
            callerStack = new Exception();
        }
        
        done = true;
        this.value = value;
        notifyAll();
    }
    
    /**
     * Sets the Exception that will be thrown by the get() method
     */
    public synchronized void setException(E exception) {
        if (exception == null) {
            throw new NullPointerException();
        }
        
        if (cancelled) {
            return;
        }
        
        if (done && oneShot) {
            throw new IllegalStateException("The OnewayExchanger is configured for a single shot");
        }
        
        if (captureCallerStack) {
            callerStack = new Exception();
        }
        
        done = true;
        this.exception = exception;
        notifyAll();
    }
    
    /**
     * Resets the OnewayExchanger so that it can be
     * reused unless it's configured for a single shot
     */
    public synchronized void reset() {
        if (oneShot) {
            throw new IllegalStateException("The OnewayExchanger is configured for a single shot");
        }
        
        done = false;
        cancelled = false;
        value = null;
        exception = null;
        callerStack = null;
    }
    
    /**
     * 
     */
    public synchronized Throwable getCallerStack() {
        return callerStack;
    }
    
    @Override
    public String toString() {
        boolean done = false;
        boolean cancelled = false;
        V value = null;
        E exception = null;
        Throwable callerStack = null;
        synchronized (this) {
            done = this.done;
            cancelled = this.cancelled;
            value = this.value;
            exception = this.exception;
            callerStack = this.callerStack;
        }
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("OnewayExchanger: ")
            .append("oneShort=").append(oneShot)
            .append(", done=").append(done)
            .append(", cancelled=").append(cancelled)
            .append(", value=").append(value)
            .append(", exception=").append(exception != null ? ExceptionUtils.toString(exception) : null)
            .append(", callerStack=").append(callerStack != null ? ExceptionUtils.toString(callerStack) : null);
        return buffer.toString();
    }
}
