package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface ValueEntity {

    public long getCreationTime();
    
    public long getAge(TimeUnit unit);
    
    public Contact getSource();

    public KUID getKey();

    public byte[] getValue();
}