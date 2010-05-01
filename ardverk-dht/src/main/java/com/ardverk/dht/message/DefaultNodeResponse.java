package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact2;

public class DefaultNodeResponse extends AbstractLookupResponse 
        implements NodeResponse {

    private final Contact2[] contacts;
    
    public DefaultNodeResponse(MessageId messageId, Contact2 contact, 
            SocketAddress address, Contact2[] contacts) {
        super(messageId, contact, address);
        
        if (contacts == null) {
            throw new NullArgumentException("contacts");
        }
        
        this.contacts = contacts;
    }

    @Override
    public Contact2[] getContacts() {
        return contacts;
    }
}
