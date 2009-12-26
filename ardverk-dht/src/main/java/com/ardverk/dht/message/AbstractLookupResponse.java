package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

abstract class AbstractLookupResponse extends AbstractResponseMessage 
        implements LookupResponse {

    public AbstractLookupResponse(MessageId messageId, Contact contact,
            Contact destination) {
        super(messageId, contact, destination);
    }

    public AbstractLookupResponse(MessageId messageId, Contact contact,
            SocketAddress address) {
        super(messageId, contact, address);
    }
}
