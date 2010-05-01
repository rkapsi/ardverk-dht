package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.NullArgumentException;
import org.ardverk.utils.StringUtils;

import com.ardverk.dht.io.LookupResponseHandler.State;

public class DefaultValueEntity extends AbstractEntity implements ValueEntity {

    private final byte[] value;
    
    public DefaultValueEntity(long time, TimeUnit unit, byte[] value) {
        super(time, unit);
        
        if (value == null) {
            throw new NullArgumentException("value");
        }
        
        this.value = value;
    }
    
    public DefaultValueEntity(State state, byte[] value) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        if (value == null) {
            throw new NullArgumentException("value");
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
