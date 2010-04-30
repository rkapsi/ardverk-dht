package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public class DefaultNodeResponse extends AbstractLookupResponse 
        implements NodeResponse {

    private final Contact2[] contacts;
    
    public DefaultNodeResponse(MessageId messageId, 
            Contact2 contact, Contact2[] contacts) {
        super(messageId, contact);
        
        if (contacts == null) {
            throw new NullPointerException("contacts");
        }
        
        this.contacts = contacts;
    }

    @Override
    public Contact2[] getContacts() {
        return contacts;
    }
}
