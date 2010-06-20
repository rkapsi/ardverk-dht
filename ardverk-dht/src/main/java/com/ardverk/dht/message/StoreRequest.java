package com.ardverk.dht.message;

import com.ardverk.dht.storage.ValueTuple;

/**
 * 
 */
public interface StoreRequest extends RequestMessage {
    
    /**
     * 
     */
    public ValueTuple getValueTuple();
}
