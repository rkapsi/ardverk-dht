package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.QueueKey;
import com.ardverk.dht.routing.Contact;

public interface Config {
    
    /**
     * Returns the {@link QueueKey}.
     */
    public QueueKey getQueueKey();
    
    /**
     * Sets the {@link QueueKey}.
     */
    public void setQueueKey(QueueKey queueKey);
    
    /**
     * Sets the timeout for the operation
     */
    public void setOperationTimeout(long timeout, TimeUnit unit);
    
    /**
     * Returns the operation timeout in the given {@link TimeUnit}.
     */
    public long getOperationTimeout(TimeUnit unit);
    
    /**
     * Returns the operation timeout in milliseconds.
     */
    public long getOperationTimeoutInMillis();
    
    /**
     * The RTT multiplier is used multiply a {@link Contact}'s RTT
     * by some number.
     * 
     * @see #getAdaptiveTimeout(Contact, long, TimeUnit)
     */
    public void setRountTripTimeMultiplier(double multiplier);
    
    /**
     * @see #getAdaptiveTimeout(Contact, long, TimeUnit)
     */
    public double getRoundTripTimeMultiplier();
    
    /**
     * Returns an <tt>adaptive</tt> timeout for the given {@link Contact}
     * which is ideally less than the given default timeout.
     */
    public long getAdaptiveTimeout(Contact dst, long defaultTimeout, TimeUnit unit);
}