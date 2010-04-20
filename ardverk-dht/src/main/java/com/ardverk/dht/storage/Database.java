package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public interface Database {
    
    public static interface Condition {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    public Condition store(Contact2 src, KUID key, byte[] value);
    
    public byte[] lookup(KUID key);
    
    public Value get(KUID key);
    
    public Value[] select(KUID key);
    
    public Value[] values();
    
    public int size();
    
    public boolean isEmpty();
}
