package com.ardverk.dht.entity;

import java.util.Collection;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface NodeEntity extends Entity {
    
    public AsyncFuture<StoreEntity> store(KUID key, byte[] value);
    
    public Collection<Contact> getContact();
}
