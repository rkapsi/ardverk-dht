package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.State;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {

    public DefaultValueEntity(long time, TimeUnit unit) {
        super(time, unit);
    }
    
    public DefaultValueEntity(State state) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
    }
}
