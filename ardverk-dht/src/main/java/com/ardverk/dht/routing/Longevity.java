package com.ardverk.dht.routing;

/**
 * Objects that have a persistent creation time and update
 * over time may implement this interface.
 */
public interface Longevity {

    /**
     * Returns the object's creation time.
     */
    public long getCreationTime();
    
    /**
     * Returns the time when this object was modified.
     */
    public long getTimeStamp();
}
