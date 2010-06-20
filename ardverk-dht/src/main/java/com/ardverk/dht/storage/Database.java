package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;

public interface Database {
    
    public static interface Condition {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    public Condition store(ValueTuple value);
    
    public ValueTuple get(KUID key);
    
    public ValueTuple[] select(KUID key);
    
    public ValueTuple[] values();
    
    public int size();
    
    public boolean isEmpty();
}
