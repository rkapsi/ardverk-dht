package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeEntity extends AbstractEntity implements NodeEntity {
    
    private final Outcome outcome;
    
    public DefaultNodeEntity(Outcome outcome) {
        super(outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
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
