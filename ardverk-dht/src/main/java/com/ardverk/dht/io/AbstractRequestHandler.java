package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.ResponseMessage;

public abstract class AbstractRequestHandler 
        extends AbstractMessageHandler implements RequestHandler {

    public AbstractRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    public void send(ResponseMessage message) throws IOException {
        messageDispatcher.send(message);
    }
}
