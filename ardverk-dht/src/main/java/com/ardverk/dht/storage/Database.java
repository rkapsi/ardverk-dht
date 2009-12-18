package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;

public interface Database {

    public byte[] get(KUID key);
    
    public byte[] put(KUID key, byte[] value);
}
