package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

abstract class AbstractValueEntity extends AbstractEntity implements ValueEntity {

    public AbstractValueEntity(long time, TimeUnit unit) {
        super(time, unit);
    }
    
    @Override
    public Contact getSender() {
        return getValueTuple().getSender();
    }
    
    @Override
    public Contact getCreator() {
        return getValueTuple().getCreator();
    }

    @Override
    public byte[] getValue() {
        return getValueTuple().getValue();
    }
    
    @Override
    public ValueTuple getValueTuple() {
        return getValueTuples()[0];
    }
}
