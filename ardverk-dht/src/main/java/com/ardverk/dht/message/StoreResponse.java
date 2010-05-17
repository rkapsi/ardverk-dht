package com.ardverk.dht.message;

import com.ardverk.dht.storage.Database.Condition;

/**
 * 
 */
public interface StoreResponse extends ResponseMessage {

    /**
     * 
     */
    public Condition getStatus();
}
