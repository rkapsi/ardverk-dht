package com.ardverk.dht.routing;

import com.ardverk.dht.Identifier;

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
