package com.ardverk.dht2;

/**
 * 
 */
public enum QueueKey {
    
    /**
     * 
     */
    SERIAL,
    
    /**
     * 
     */
    PARALLEL;
    
    /**
     * 
     */
    public static final QueueKey DEFAULT = QueueKey.PARALLEL;
    
    /**
     * 
     */
    public static final QueueKey BACKEND = QueueKey.SERIAL;
}
