package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface Value {

    public long getCreationTime();
    
    public long getAge(TimeUnit unit);
    
    public long getAgeInMillis();
    
    public Contact getSource();

    public KUID getKey();

    public byte[] getValue();
}