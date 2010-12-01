package com.ardverk.dht.message;

import com.ardverk.dht.KUID;

/**
 * 
 */
public interface ValueRequest extends LookupRequest {

    /**
     * 
     */
    public KUID getKey();
}
