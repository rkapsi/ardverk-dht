package com.ardverk.dht.routing;

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
