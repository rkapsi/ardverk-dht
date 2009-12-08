package com.ardverk.dht.io;

public abstract class RequestHandler extends AbstractMessageHandler {
    
    public RequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
}
