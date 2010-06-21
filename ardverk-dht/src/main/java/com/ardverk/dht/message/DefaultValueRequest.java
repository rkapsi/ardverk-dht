package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Key;

public class DefaultValueRequest extends AbstractLookupRequest 
        implements ValueRequest {
    
    private final Key key;
    
    public DefaultValueRequest(MessageId messageId, Contact contact, 
            SocketAddress address, Key key) {
        super(messageId, contact, address);
        
        this.key = Arguments.notNull(key, "key");
    }
    
    @Override
    public Key getKey() {
        return key;
    }
}