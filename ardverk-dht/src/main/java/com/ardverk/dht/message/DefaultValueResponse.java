package com.ardverk.dht.message;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact2;

public class DefaultValueResponse extends AbstractLookupResponse 
        implements ValueResponse {

    private final byte[] value;
    
    public DefaultValueResponse(MessageId messageId, 
            Contact2 contact, byte[] value) {
        super(messageId, contact);
        
        if (value == null) {
            throw new NullArgumentException("value");
        }
        
        this.value = value;
    }
    
    @Override
    public byte[] getValue() {
        return value;
    }
}
