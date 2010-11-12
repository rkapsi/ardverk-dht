package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;

public class DefaultRefreshEntity extends AbstractEntity implements RefreshEntity {

    private final ArdverkFuture<PingEntity>[] pingFutures;
    
    private final ArdverkFuture<NodeEntity>[] lookupFutures;
    
    public DefaultRefreshEntity(ArdverkFuture<PingEntity>[] pingFutures, 
            ArdverkFuture<NodeEntity>[] lookupFutures, long time, TimeUnit unit) {
        super(time, unit);
        
        this.pingFutures = pingFutures;
        this.lookupFutures = lookupFutures;
    }

    @Override
    public ArdverkFuture<PingEntity>[] getPingFutures() {
        return pingFutures;
    }

    @Override
    public ArdverkFuture<NodeEntity>[] getLookupFutures() {
        return lookupFutures;
    }
}
