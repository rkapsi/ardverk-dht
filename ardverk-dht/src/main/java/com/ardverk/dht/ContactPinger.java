package com.ardverk.dht;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact2;

public interface ContactPinger {

    public ArdverkFuture<PingEntity> ping(Contact2 contact, 
            long timeout, TimeUnit unit);
}
