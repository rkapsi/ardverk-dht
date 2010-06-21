package com.ardverk.dht.storage;

public interface Value {
    
    public byte[] getId();
    
    public Key getKey();
    
    public byte[] getValue();
    
    public int size();
    
    public boolean isEmpty();
}
