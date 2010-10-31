package com.ardverk.dht.entity;

import com.ardverk.dht2.Contacts;

public interface NodeEntity extends LookupEntity, Contacts {
    
    //public AsyncFuture<StoreEntity> store(Contact creator, KUID key, byte[] value);
    
    public int getHop();
}
