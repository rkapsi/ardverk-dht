package com.ardverk.dht2;

import com.ardverk.dht.routing.Contact;

public interface Contacts extends Iterable<Contact> {

    public int size();
    
    public Contact getContact(int index);
    
    public Contact[] getContacts();
}
