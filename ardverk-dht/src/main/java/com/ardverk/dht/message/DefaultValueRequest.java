package com.ardverk.dht.message;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public class DefaultValueRequest extends AbstractLookupRequest 
        implements ValueRequest {
    
    public DefaultValueRequest(MessageId messageId, 
            Contact2 contact, KUID key) {
        super(messageId, contact, key);
    }
}