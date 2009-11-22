package com.ardverk.dht.io.transport;

import java.io.IOException;

import com.ardverk.dht.io.session.SessionContext;

public interface TransportListener {
    
    public void received(SessionContext session, Object message) throws IOException;
}