package com.ardverk.dht.message;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

abstract class AbstractLookupRequest extends AbstractRequestMessage 
        implements LookupRequest {
    
    private final KUID key;
    
    public AbstractLookupRequest(MessageId messageId, 
            Contact2 contact, KUID key) {
        super(messageId, contact);
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        this.key = key;
    }

    @Override
    public KUID getKey() {
        return key;
    }
}
