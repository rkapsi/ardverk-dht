package com.ardverk.dht.entity;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

public interface ValueEntity extends LookupEntity {
    
    public Contact getSender();
    
    public Contact getCreator();
    
    public byte[] getValue();
    
    public ValueTuple getValueTuple();
    
    public ValueTuple[] getValueTuples();
}
