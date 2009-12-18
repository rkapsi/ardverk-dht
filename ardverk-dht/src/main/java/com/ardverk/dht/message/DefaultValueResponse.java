package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultValueResponse extends AbstractResponseMessage 
        implements ValueResponse {

    private final byte[] value;
    
    public DefaultValueResponse(
            MessageId messageId, Contact contact, 
            Contact destination, byte[] value) {
        super(messageId, contact, destination);
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.value = value;
    }
    
    public DefaultValueResponse(
            MessageId messageId, Contact contact, 
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
