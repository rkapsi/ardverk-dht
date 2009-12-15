package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultNodeResponse extends AbstractResponseMessage 
        implements NodeResponse {

    private final Contact[] contacts;
    
    public DefaultNodeResponse(
            MessageId messageId, Contact contact, 
            Contact destination, Contact[] contacts) {
        super(messageId, contact, destination);
        
        if (contacts == null) {
            throw new NullPointerException("contacts");
        }
        
        this.contacts = contacts;
    }
    
    public DefaultNodeResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address, Contact[] contacts) {
        super(messageId, contact, address);
        
        if (contacts == null) {
            throw new NullPointerException("contacts");
        }
        
        this.contacts = contacts;
    }

    @Override
    public Contact[] getContacts() {
        return contacts;
    }
}
