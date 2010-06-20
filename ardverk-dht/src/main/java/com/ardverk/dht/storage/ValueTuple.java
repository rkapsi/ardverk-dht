package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface ValueTuple {

    public long getCreationTime();
    
    public long getAge(TimeUnit unit);
    
    public long getAgeInMillis();
    
    public Contact getSender();
    
    public Contact getCreator();

    public KUID getPrimaryKey();
    
    public KUID getSecondaryKey();

    public Value getValue();
    
    public boolean isEmpty();
}