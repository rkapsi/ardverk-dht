package com.ardverk.dht2;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;

public class DefaultContacts extends AbstractContacts {

    private final Contact[] contacts;
    
    private final int offset;
    
    private final int length;
    
    public DefaultContacts(Contact[] contacts) {
        this(contacts, 0, contacts.length);
    }
    
    public DefaultContacts(Contact[] contacts, int offset, int length) {
        this.contacts = Arguments.notNull(contacts, "contacts");
        
        if (offset < 0 || length < 0 
                || contacts.length < (offset+length)) {
            throw new IllegalArgumentException(
                    "offset=" + offset + ", length=" + length);
        }
        
        this.offset = offset;
        this.length = length;
    }
    
    @Override
    public int size() {
        return length;
    }
    
    @Override
    public Contact get(int index) {
        int pos = offset + index;
        if (pos < offset || (offset + length) < pos) {
            throw new IllegalArgumentException("index=" + index);
        }
        
        return contacts[pos];
    }
    
    @Override
    public Contact[] toArray() {
        return contacts;
    }
}
