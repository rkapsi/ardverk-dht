package com.ardverk.dht.entity;

import com.ardverk.dht.message.StoreResponse;

public interface StoreEntity extends Entity {
    
    public StoreResponse[] getStoreResponses();
}
