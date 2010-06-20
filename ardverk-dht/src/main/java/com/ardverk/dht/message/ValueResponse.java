package com.ardverk.dht.message;

import com.ardverk.dht.storage.ValueTuple;

/**
 * 
 */
public interface ValueResponse extends LookupResponse {
    
    /**
     * 
     */
    public ValueTuple getValueTuple();
}
