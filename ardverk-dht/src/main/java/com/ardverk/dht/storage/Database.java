package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;

public interface Database {
    
    public static interface Condition {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    public Condition store(Value value);
    
    public Value get(KUID key);
    
    public Value[] select(KUID key);
    
    public Value[] values();
    
    public int size();
    
    public boolean isEmpty();
}
