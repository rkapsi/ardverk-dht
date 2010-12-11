package com.ardverk.dht.entity;

import com.ardverk.dht.concurrent.ArdverkFuture;

public interface SyncEntity extends Entity {
    
    public ArdverkFuture<StoreEntity>[] getStoreFutures();
}