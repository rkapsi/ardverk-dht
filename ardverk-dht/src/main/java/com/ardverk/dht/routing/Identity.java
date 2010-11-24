package com.ardverk.dht.routing;

/**
 * 
 */
public interface Identity extends Identifier {

    /**
     * Returns the creation time
     */
    public long getCreationTime();
    
    /**
     * Returns the time stamp
     */
    public long getTimeStamp();
}
