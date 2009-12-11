package com.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreResponse;

public class StoreResponseHandler extends ResponseHandler<StoreEntity> {

    private final NodeEntity entity;
    
    private final KUID key;
    
    private final byte[] value;
    
    private final List<StoreResponse> responses 
        = new ArrayList<StoreResponse>();
    
    public StoreResponseHandler(
            MessageDispatcher messageDispatcher, 
            NodeEntity entity, KUID key, byte[] value) {
        super(messageDispatcher);
        
        if (entity == null) {
            throw new NullPointerException("entity");
        }
        
        if (key == null) {
            throw new NullPointerException("key");
        }
        
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        this.entity = entity;
        this.key = key;
        this.value = value;
    }

    @Override
    protected void go(AsyncFuture<StoreEntity> future) throws Exception {
        
    }

    @Override
    protected void processResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        
        StoreResponse message = (StoreResponse)response;
    }

    @Override
    protected void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        setException(new TimeoutIoException(request, time, unit));
    }
}
