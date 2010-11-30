package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;

public class DefaultNodeResponse extends AbstractLookupResponse 
        implements NodeResponse {

    private final Contact[] contacts;
    
    public DefaultNodeResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Contact[] contacts) {
        super(messageId, contact, address);
        
        this.contacts = Arguments.notNull(contacts, "contacts");
    }

    @Override
    public Contact[] getContacts() {
        return contacts;
    }
}
