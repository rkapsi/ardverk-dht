package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;

public interface Database {
    
    public static interface Status {
        
        public boolean isSuccess();
        
        public String name();
    }
    
    public Status store(Contact src, KUID key, byte[] value);
    
    public byte[] lookup(KUID key);
    
    public ValueEntity get(KUID key);
    
    public int size();
    
    public boolean isEmpty();
}
