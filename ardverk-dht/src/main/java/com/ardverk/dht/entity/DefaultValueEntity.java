package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

public class DefaultValueEntity extends AbstractLookupEntity implements ValueEntity {
    
    private final Outcome outcome;
    
    private final ValueTuple[] values;
    
    public DefaultValueEntity(Outcome outcome, ValueTuple[] values) {
        super(outcome.getLookupId(), outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.outcome = outcome;
        this.values = values;
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
    
    @Override
    public ValueTuple[] getValueTuples() {
        return values;
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
}
