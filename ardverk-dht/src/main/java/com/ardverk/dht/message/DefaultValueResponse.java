package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultValueResponse extends AbstractResponseMessage 
        implements ValueResponse {

    public DefaultValueResponse(
            MessageId messageId, Contact contact, 
            Contact destination) {
        super(messageId, contact, destination);
    }
    
    public DefaultValueResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address) {
        super(messageId, contact, address);
    }
}
