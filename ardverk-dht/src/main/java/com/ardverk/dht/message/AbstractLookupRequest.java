package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

abstract class AbstractLookupRequest extends AbstractRequestMessage 
        implements LookupRequest {
    
    public AbstractLookupRequest(MessageId messageId, Contact contact, 
            SocketAddress address) {
        super(messageId, contact, address);
    }
}
