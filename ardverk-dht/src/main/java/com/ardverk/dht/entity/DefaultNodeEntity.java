package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.LookupResponseHandler.State;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeEntity extends AbstractEntity implements NodeEntity {

    private final AsyncProcessExecutorService executor = null;
    
    private final MessageDispatcher messageDispatcher = null;
    
    private final Contact[] contacts;
    
    private final int hop;
    
    public DefaultNodeEntity(Contact[] contacts, int hops, long time, TimeUnit unit) {
        super(time, unit);
        
        if (contacts == null) {
            throw new NullArgumentException("contacts");
        }
        
        this.contacts = contacts;
        this.hop = hops;
    }
    
    public DefaultNodeEntity(State state) {
        super(state.getTimeInMillis(), TimeUnit.MILLISECONDS);
        
        this.contacts = state.getContacts();
        this.hop = state.getHop();
    }
    
    /*@Override
    public AsyncProcessFuture<StoreEntity> store(Contact creator, KUID key, byte[] value) {
        AsyncProcess<StoreEntity> process = new StoreResponseHandler(
                messageDispatcher, this, creator, key, value);
        return executor.submit(process);
    }*/
    
    @Override
    public Contact getContact(int index) {
        return contacts[index];
    }

    @Override
    public int size() {
        return contacts.length;
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
