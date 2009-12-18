package com.ardverk.dht.entity;

public interface ValueEntity extends LookupEntity {
    
    public byte[] getValue();
    
    public String getValueAsString();
}
