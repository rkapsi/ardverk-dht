package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractMessage implements Message {

    private final long creationTime = System.currentTimeMillis();

    private final Contact contact;
    
    public AbstractMessage(Contact contact) {
        if (contact == null) {
            throw new NullPointerException("contact");
        }
        
        this.contact = contact;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public Contact getContact() {
        return contact;
    }
}
