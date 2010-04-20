package com.ardverk.dht;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact2;

public interface ContactPinger {

    public AsyncFuture<PingEntity> ping(Contact2 contact);
}
