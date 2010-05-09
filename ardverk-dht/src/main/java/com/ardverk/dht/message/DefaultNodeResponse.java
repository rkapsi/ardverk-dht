package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact;

public class DefaultNodeResponse extends AbstractLookupResponse 
        implements NodeResponse {

    private final Contact[] contacts;
    
    public DefaultNodeResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Contact[] contacts) {
        super(messageId, contact, address);
        
        if (contacts == null) {
            throw new NullArgumentException("contacts");
        }
        
        this.contacts = contacts;
    }

    @Override
    public Contact[] getContacts() {
        return contacts;
    }
}
