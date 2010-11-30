package com.ardverk.dht.message;

import com.ardverk.dht.storage.Key;

/**
 * 
 */
public interface ValueRequest extends LookupRequest {

    /**
     * 
     */
    public Key getKey();
}
