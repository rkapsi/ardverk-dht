package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;
import com.ardverk.dht.storage.ValueTuple;

public class DefaultValueEntity extends AbstractValueEntity {
    
    private final Outcome outcome;
    
    private final ValueTuple[] values;
    
    public DefaultValueEntity(Outcome outcome, ValueTuple[] values) {
        super(outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.outcome = outcome;
        this.values = values;
    }
    
    @Override
    public ValueTuple[] getValueTuples() {
        return values;
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
}
