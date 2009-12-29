package com.ardverk.dht.io;

public abstract class AbstractRequestHandler 
        extends AbstractMessageHandler implements RequestHandler {

    public AbstractRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
}
