package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

abstract class AbstractLookupResponse extends AbstractResponseMessage 
        implements LookupResponse {
    
    public AbstractLookupResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) {
        super(messageId, contact, address);
    }
}
