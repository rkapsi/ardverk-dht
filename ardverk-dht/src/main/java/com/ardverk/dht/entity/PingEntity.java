package com.ardverk.dht.entity;

import com.ardverk.dht.routing.Contact;

public interface PingEntity extends Entity {
    
    /**
     * Returns the remote {@link Contact}
     */
    public Contact getContact();
}
