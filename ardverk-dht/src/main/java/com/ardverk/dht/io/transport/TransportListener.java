package com.ardverk.dht.io.transport;

import java.io.IOException;

import com.ardverk.dht.io.session.Session;

public interface TransportListener {
    
    public void received(Session session, Object message) throws IOException;
}