package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface Database {
    
    public static interface Condition {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    public Condition store(Contact src, KUID key, byte[] value);
    
    public byte[] lookup(KUID key);
    
    public ValueEntity get(KUID key);
    
    public ValueEntity[] select(KUID key);
    
    public ValueEntity[] values();
    
    public int size();
    
    public boolean isEmpty();
}
