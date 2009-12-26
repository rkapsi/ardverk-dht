package com.ardverk.dht.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public class DefaultDatabase implements Database {

    private final Map<KUID, byte[]> database 
        = new ConcurrentHashMap<KUID, byte[]>();
    
    @Override
    public byte[] get(KUID key) {
        return database.get(key);
    }

    @Override
    public byte[] put(Contact src, KUID key, byte[] value) {
        return database.put(key, value);
    }
}
