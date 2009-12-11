package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public class StoreResponseHandler extends ResponseHandler<StoreEntity> {

    private final KUID key;
    
    private final byte[] value;
    
    private final NodeEntity entity;
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            KUID key, byte[] value) {
        this(messageDispatcher, null, key, value);
    }
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            NodeEntity entity,
            KUID key, byte[] value) {
        super(messageDispatcher);
        
        this.entity = entity;
        this.key = key;
        this.value = value;
    }

    @Override
    protected void go(AsyncFuture<StoreEntity> future) throws Exception {
        if (entity == null) {
            // DO LOOKUP
        } else {
            store(entity);
        }
    }
    
    private void store(NodeEntity entity) {
        
    }

    @Override
    protected void processResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
    }

    @Override
    protected void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
    }
}
