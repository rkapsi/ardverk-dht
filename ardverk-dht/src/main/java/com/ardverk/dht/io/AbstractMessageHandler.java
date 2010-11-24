package com.ardverk.dht.io;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.message.MessageFactory;

public abstract class AbstractMessageHandler {

    protected final MessageDispatcher messageDispatcher;
    
    public AbstractMessageHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = Arguments.notNull(messageDispatcher, "messageDispatcher");
    }
    
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
    
    public MessageFactory getMessageFactory() {
        return messageDispatcher.getMessageFactory();
    }
}
