package com.ardverk.dht.io;

public abstract class AbstractMessageHandler {

    protected final MessageDispatcher messageDispatcher;
    
    public AbstractMessageHandler(MessageDispatcher messageDispatcher) {
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        
        this.messageDispatcher = messageDispatcher;
    }
    
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
