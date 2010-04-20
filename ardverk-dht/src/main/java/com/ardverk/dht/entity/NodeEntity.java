package com.ardverk.dht.entity;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public interface NodeEntity extends LookupEntity {
    
    public AsyncFuture<StoreEntity> store(KUID key, byte[] value);
    
    public int size();
    
    public Contact2 getContact(int index);
    
    public Contact2[] getContacts();
    
    public int getHop();
}
