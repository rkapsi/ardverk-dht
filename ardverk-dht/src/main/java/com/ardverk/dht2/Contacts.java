package com.ardverk.dht2;

import com.ardverk.dht.routing.Contact;

public interface Contacts extends Iterable<Contact> {

    public int size();
    
    public boolean isEmpty();
    
    public Contact get(int index);
    
    public Contact[] toArray();
}
