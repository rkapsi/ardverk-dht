package com.ardverk.dht;

import com.ardverk.concurrent.AsyncFutureListener;
import com.ardverk.dht.routing.Contact;

public interface ContactPinger {

    public boolean ping(Contact contact, AsyncFutureListener<?> l);
}
