package com.ardverk.dht.entity;

import com.ardverk.dht.routing.Contact;

public interface NodeEntity extends LookupEntity {
    
    //public AsyncFuture<StoreEntity> store(Contact creator, KUID key, byte[] value);
    
    public int size();
    
    public Contact getContact(int index);
    
    public Contact[] getContacts();
    
    public int getHop();
}
