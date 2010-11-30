package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;

public interface RequestHandler {
    
    public void handleRequest(RequestMessage request) throws IOException;
}
