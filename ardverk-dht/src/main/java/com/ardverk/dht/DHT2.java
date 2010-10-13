package com.ardverk.dht;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;

public interface DHT2 {

    public ArdverkFuture<PingEntity> ping(PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(LookupConfig config);
    
    public ArdverkFuture<StoreEntity> put(StoreConfig config);
    
    public ArdverkFuture<ValueEntity> get(ValueConfig config);
    
    public static interface ArdverkConfig {
        
    }
    
    public static interface PingConfig extends ArdverkConfig {
        
    }
    
    public static interface LookupConfig extends ArdverkConfig {
        
    }
    
    public static interface ValueConfig extends ArdverkConfig {
        
    }
    
    public static interface StoreConfig extends ArdverkConfig {
        
    }
}
