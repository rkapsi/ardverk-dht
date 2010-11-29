package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.QueueKey;
import com.ardverk.dht.routing.Contact;

public interface Config {
    
    public QueueKey getQueueKey();
    
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
     * Returns an <tt>adaptive</tt> timeout for the given {@link Contact}
     * which is ideally less than the given default timeout.
     */
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit);
}