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
    
    @Override
    public int hashCode() {
        return primaryKey.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Key)) {
            return false;
        }
        
        Key other = (Key)o;
        return primaryKey.equals(other.getPrimaryKey());
    }
    
    @Override
    public String toString() {
        return primaryKey.toString();
    }
}
