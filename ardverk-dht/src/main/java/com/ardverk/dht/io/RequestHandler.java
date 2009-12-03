package com.ardverk.dht.io;

import com.ardverk.dht.message.RequestMessage;

public abstract class RequestHandler<T extends RequestMessage> 
        extends AbstractMessageHandler<T> {
    
    public RequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
}
