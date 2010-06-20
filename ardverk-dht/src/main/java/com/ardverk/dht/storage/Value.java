package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface Value {

    public long getCreationTime();
    
    public long getAge(TimeUnit unit);
    
    public long getAgeInMillis();
    
    public byte[] getId();
    
    public Contact getSender();
    
    public Contact getCreator();

    public KUID getPrimaryKey();
    
    public KUID getSecondaryKey();

    public byte[] getValue();
    
    public int size();
    
    public boolean isEmpty();
}