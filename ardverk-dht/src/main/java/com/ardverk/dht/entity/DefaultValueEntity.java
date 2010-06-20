package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.io.LookupResponseHandler.State;
import com.ardverk.dht.storage.Value;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {
    
    private final Value value;
    
    public DefaultValueEntity(Value value, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.value = Arguments.notNull(value, "value");
    }
    
    public DefaultValueEntity(State state, Value value) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.value = Arguments.notNull(value, "value");
    }

    @Override
    public Value getValue() {
        return value;
    }
}
