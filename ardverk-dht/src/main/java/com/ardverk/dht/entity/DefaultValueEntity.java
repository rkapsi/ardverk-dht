package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.State;
import com.ardverk.utils.StringUtils;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {

    private final byte[] value;
    
    public DefaultValueEntity(long time, TimeUnit unit, byte[] value) {
        super(time, unit);
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.value = value;
    }
    
    public DefaultValueEntity(State state, byte[] value) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return value;
    }
    
    @Override
    public String getValueAsString() {
        return StringUtils.toString(value);
    }
}
