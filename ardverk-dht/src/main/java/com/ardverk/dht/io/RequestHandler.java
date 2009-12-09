package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;

public abstract class RequestHandler extends AbstractMessageHandler {
    
    public RequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    public abstract void handleRequest(RequestMessage request) throws IOException;
}
