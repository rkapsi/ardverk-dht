package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

abstract class AbstractLookupResponse extends AbstractResponseMessage 
        implements LookupResponse {
    
    public AbstractLookupResponse(MessageId messageId, Contact2 contact) {
        super(messageId, contact);
    }
}
