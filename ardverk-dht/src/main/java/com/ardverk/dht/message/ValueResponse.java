package com.ardverk.dht.message;

import com.ardverk.dht.storage.Value;

/**
 * 
 */
public interface ValueResponse extends LookupResponse {
    
    /**
     * 
     */
    public Value getValue();
}
