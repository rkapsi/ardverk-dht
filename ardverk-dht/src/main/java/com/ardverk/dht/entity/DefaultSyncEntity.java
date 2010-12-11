package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;

public class DefaultSyncEntity extends AbstractEntity implements SyncEntity {

    private final ArdverkFuture<StoreEntity>[] futures;
    
    @SuppressWarnings("unchecked")
    public DefaultSyncEntity(long time, TimeUnit unit) {
        this(new ArdverkFuture[0], time, unit);
    }
    
    public DefaultSyncEntity(ArdverkFuture<StoreEntity>[] futures, 
            long time, TimeUnit unit) {
        super(time, unit);
        this.futures = futures;
    }

    @Override
    public ArdverkFuture<StoreEntity>[] getStoreFutures() {
        return futures;
    }
}