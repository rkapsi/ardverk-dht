package com.ardverk.dht.io;

import java.io.IOException;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.LookupEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public class StoreResponseHandler extends ResponseHandler<StoreEntity> {

    private final KUID key;
    
    private final byte[] value;
    
    private final LookupEntity entity;
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            KUID key, byte[] value) {
        this(messageDispatcher, null, key, value);
    }
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            LookupEntity entity,
            KUID key, byte[] value) {
        super(messageDispatcher);
        
        this.entity = entity;
        this.key = key;
        this.value = value;
    }

    @Override
    protected void innerStart(AsyncFuture<StoreEntity> future) throws Exception {
        if (entity == null) {
            // DO LOOKUP
        } else {
            store(entity);
        }
    }
    
    private void store(LookupEntity entity) {
        
    }

    @Override
    public void handleResponse(ResponseMessage response) throws Exception {
    }

    @Override
    public void handleTimeout(RequestMessage request) throws IOException {
    }
}
