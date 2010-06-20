package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Value;

public class DefaultValueResponse extends AbstractLookupResponse 
        implements ValueResponse {
    
    private final Value value;
    
    public DefaultValueResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Value value) {
        super(messageId, contact, address);
        
        this.value = Arguments.notNull(value, "value");
    }
    
    @Override
    public Value getValue() {
        return value;
    }
}
