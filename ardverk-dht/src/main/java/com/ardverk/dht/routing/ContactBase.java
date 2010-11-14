package com.ardverk.dht.routing;

import com.ardverk.dht.KUID;

/**
 * 
 */
public interface ContactBase {

    /**
     * Returns the creation time
     */
    public long getCreationTime();
    
    /**
     * Returns the time stamp
     */
    public long getTimeStamp();
    
    /**
     * Returns the {@link KUID}
     */
    public KUID getContactId();
}
