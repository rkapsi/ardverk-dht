package com.ardverk.dht.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.routing.Contact;

public class NodeEntity extends AbstractEntity {

    private final AsyncExecutorService executor = null;
    
    private final MessageDispatcher messageDispatcher = null;
    
    public NodeEntity(long time, TimeUnit unit) {
        super(time, unit);
    }
    
    public AsyncFuture<StoreEntity> store(KUID key, byte[] value) {
        AsyncProcess<StoreEntity> process = new StoreResponseHandler(
                messageDispatcher, this, key, value);
        return executor.submit(process);
    }
    
    public Collection<Contact> getContact() {
        return Collections.emptySet();
    }
}
