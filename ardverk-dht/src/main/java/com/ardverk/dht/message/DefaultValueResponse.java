package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact;

public class DefaultValueResponse extends AbstractLookupResponse 
        implements ValueResponse {

    private final byte[] value;
    
    public DefaultValueResponse(MessageId messageId, Contact contact, 
            SocketAddress address, byte[] value) {
        super(messageId, contact, address);
        
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
