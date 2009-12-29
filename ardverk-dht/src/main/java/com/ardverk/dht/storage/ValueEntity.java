package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface ValueEntity {

    public long getCreationTime();
    
    public Contact getSource();

    public KUID getKey();

    public byte[] getValue();
}