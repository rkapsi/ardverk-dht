package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.io.LookupResponseHandler.State;
import com.ardverk.dht.storage.ValueTuple;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {
    
    private final ValueTuple value;
    
    public DefaultValueEntity(ValueTuple value, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.value = Arguments.notNull(value, "value");
    }
    
    public DefaultValueEntity(State state, ValueTuple value) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.value = Arguments.notNull(value, "value");
    }

    @Override
    public ValueTuple getValue() {
        return value;
    }
}
