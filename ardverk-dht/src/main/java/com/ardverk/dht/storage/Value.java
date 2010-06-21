package com.ardverk.dht.storage;

public interface Value {
    
    public byte[] getId();
    
    public byte[] getValue();
    
    public int size();
    
    public boolean isEmpty();
}
