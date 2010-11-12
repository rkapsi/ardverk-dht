package com.ardverk.dht.entity;

import com.ardverk.dht.concurrent.ArdverkFuture;

public interface RefreshEntity extends Entity {

    public ArdverkFuture<PingEntity>[] getPingFutures();
    
    public ArdverkFuture<NodeEntity>[] getLookupFutures();
}
