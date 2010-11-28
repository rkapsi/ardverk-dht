package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.StoreResponse;

public class DefaultStoreEntity extends AbstractEntity implements StoreEntity {

    private final StoreResponse[] responses;
    
    public DefaultStoreEntity(StoreResponse[] responses, 
            long time, TimeUnit unit) {
        super(time, unit);
        
        this.responses = responses;
    }

    @Override
    public StoreResponse[] getStoreResponses() {
        return responses;
    }
}
