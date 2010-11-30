package com.ardverk.dht.entity;

import com.ardverk.dht.storage.ValueTuple;

public interface ValueEntity extends LookupEntity {
    
    public ValueTuple getValue();
}
