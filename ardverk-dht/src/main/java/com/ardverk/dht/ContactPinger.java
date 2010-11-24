package com.ardverk.dht;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact;

/**
 * 
 */
public interface ContactPinger {

    /**
     * Sends a ping to the given host.
     */
    public ArdverkFuture<PingEntity> ping(Contact contact, PingConfig config);
}
