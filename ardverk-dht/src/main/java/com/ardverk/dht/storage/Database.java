package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface Database {

    public byte[] get(KUID key);
    
    public byte[] store(Contact src, KUID key, byte[] value);
}
