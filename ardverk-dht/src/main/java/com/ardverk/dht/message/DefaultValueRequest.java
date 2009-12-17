package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultValueRequest extends AbstractLookupRequest 
        implements ValueRequest {
    
    public DefaultValueRequest(MessageId messageId, 
            Contact contact, Contact destination, KUID key) {
        super(messageId, contact, destination, key);
    }

    public DefaultValueRequest(MessageId messageId, 
            Contact contact, SocketAddress address, KUID key) {
        super(messageId, contact, address, key);
    }
}