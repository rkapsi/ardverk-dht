package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {
    
    public AbstractRequestMessage(
            MessageId messageId, Contact2 contact) {
        super(messageId, contact);
    }
}
