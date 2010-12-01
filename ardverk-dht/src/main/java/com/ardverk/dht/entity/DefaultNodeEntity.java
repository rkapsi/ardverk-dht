package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeEntity extends AbstractLookupEntity implements NodeEntity {
    
    private final Outcome outcome;
    
    public DefaultNodeEntity(Outcome outcome) {
        super(outcome.getLookupId(), 
                outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.outcome = outcome;
    }
    
    @Override
    public Contact[] getContacts() {
        return outcome.getContacts();
    }

    @Override
    public int getHop() {
        return outcome.getHop();
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
}
