package com.ardverk.dht.message;

import com.ardverk.dht.storage.Value;

/**
 * 
 */
public interface StoreRequest extends RequestMessage {
    
    /**
     * 
     */
    public Value getValue();
}
