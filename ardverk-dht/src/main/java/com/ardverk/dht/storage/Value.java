package com.ardverk.dht.storage;

import com.ardverk.dht.KUID;

public interface Value {
    
    public byte[] getId();
    
    public KUID getKey();
    
    public byte[] getValue();
    
    public int size();
    
    public boolean isEmpty();
}
