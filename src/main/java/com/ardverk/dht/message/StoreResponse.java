package com.ardverk.dht.message;

import com.ardverk.dht.storage.Database.Condition;

/**
 * 
 */
public interface StoreResponse extends ResponseMessage {

    /**
     * Returns the remote host's {@link Condition}.
     */
    public Condition getCondition();
}
