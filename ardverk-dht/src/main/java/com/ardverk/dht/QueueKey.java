package com.ardverk.dht;

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
