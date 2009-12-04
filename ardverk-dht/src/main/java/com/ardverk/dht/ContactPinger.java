package com.ardverk.dht;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact;

public interface ContactPinger {

    public AsyncFuture<PingEntity> ping(Contact contact);
}
