package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.State;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeEntity extends AbstractEntity implements NodeEntity {
    
    private final Contact[] contacts;
    
    private final int hop;
    
    public DefaultNodeEntity(Contact[] contacts, int hops, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.contacts = contacts;
        this.hop = hops;
    }
    
    public DefaultNodeEntity(State state) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.contacts = state.getContacts();
        this.hop = state.getHop();
    }
    
    @Override
    public Contact[] getContacts() {
        return contacts;
    }

    @Override
    public int getHop() {
        return hop;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + hop + ", " + time + ", " + unit + ")";
    }
}
