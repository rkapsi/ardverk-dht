package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public class DefaultValueResponse extends AbstractLookupResponse 
        implements ValueResponse {

    private final byte[] value;
    
    public DefaultValueResponse(MessageId messageId, 
            Contact2 contact, byte[] value) {
        super(messageId, contact);
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.value = value;
    }
    
    @Override
    public byte[] getValue() {
        return value;
    }
}
