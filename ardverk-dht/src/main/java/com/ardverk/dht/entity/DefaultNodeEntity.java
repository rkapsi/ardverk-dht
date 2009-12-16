package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.routing.Contact;

public class DefaultNodeEntity extends AbstractEntity implements NodeEntity {

    private final AsyncExecutorService executor = null;
    
    private final MessageDispatcher messageDispatcher = null;
    
    private final Contact[] contacts;
    
    public DefaultNodeEntity(Contact[] contacts, long time, TimeUnit unit) {
        super(time, unit);
        
        if (contacts == null) {
            throw new NullPointerException("contacts");
        }
        
        this.contacts = contacts;
    }
    
    @Override
    public AsyncFuture<StoreEntity> store(KUID key, byte[] value) {
        AsyncProcess<StoreEntity> process = new StoreResponseHandler(
                messageDispatcher, this, key, value);
        return executor.submit(process);
    }
    
    @Override
    public Contact[] getContact() {
        return contacts;
    }
}
