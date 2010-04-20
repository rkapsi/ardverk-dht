package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public interface Value {

    public long getCreationTime();
    
    public long getAge(TimeUnit unit);
    
    public Contact2 getSource();

    public KUID getKey();

    public byte[] getValue();
}