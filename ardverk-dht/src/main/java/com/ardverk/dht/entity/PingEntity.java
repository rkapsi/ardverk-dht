package com.ardverk.dht.entity;

import com.ardverk.dht.routing.Contact2;

public interface PingEntity extends Entity {
    
    /**
     * Returns the remote {@link Contact}
     */
    public Contact2 getContact();
}
