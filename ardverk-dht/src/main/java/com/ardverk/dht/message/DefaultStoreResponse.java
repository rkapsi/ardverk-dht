package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            Contact destination) {
        super(messageId, contact, destination);
    }
    
    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address) {
        super(messageId, contact, address);
    }
}