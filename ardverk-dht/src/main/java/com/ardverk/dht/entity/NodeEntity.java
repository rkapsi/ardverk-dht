package com.ardverk.dht.entity;

import com.ardverk.dht.routing.Contact;

public interface NodeEntity extends LookupEntity {
    
    public int getHop();
    
    public Contact[] getContacts();
}
