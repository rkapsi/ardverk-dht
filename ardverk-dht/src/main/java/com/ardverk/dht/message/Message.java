package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

/**
 * 
 */
public interface Message {
    
    /**
     * Returns the unique identifier of the {@link Message}
     */
    public MessageId getMessageId();
    
    /**
     * Returns the origin of the {@link Message}
     */
    public Contact2 getContact();
}
