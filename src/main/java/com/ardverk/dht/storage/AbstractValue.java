package com.ardverk.dht.storage;


abstract class AbstractValue implements Value {
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
