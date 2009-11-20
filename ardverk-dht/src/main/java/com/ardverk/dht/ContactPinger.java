package com.ardverk.dht;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public interface ContactPinger {

    public AsyncFuture<PingResponse> ping(Contact contact);
}
