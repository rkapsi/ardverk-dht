package com.ardverk.dht.io;

import com.ardverk.dht.message.Message;

public abstract class AbstractMessageHandler<T extends Message> 
        implements MessageHandler<T> {

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
