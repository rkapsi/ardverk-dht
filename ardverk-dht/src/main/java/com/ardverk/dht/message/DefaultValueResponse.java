package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

public class DefaultValueResponse extends AbstractLookupResponse 
        implements ValueResponse {

    private final byte[] value;
    
    public DefaultValueResponse(
            MessageId messageId, Contact2 contact, 
            Contact2 destination, byte[] value) {
        super(messageId, contact, destination);
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.value = value;
    }
    
    public DefaultValueResponse(
            MessageId messageId, Contact2 contact, 
            SocketAddress address, byte[] value) {
        super(messageId, contact, address);
        
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
