package com.ardverk.dht.storage;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;

public class DefaultKey implements Key {

    private final KUID primaryKey;
    
    public DefaultKey(KUID primaryKey) {
        this.primaryKey = Arguments.notNull(primaryKey, "primaryKey");
    }
    
    @Override
    public KUID getPrimaryKey() {
        return primaryKey;
    }
}
