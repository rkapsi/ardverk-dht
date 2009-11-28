package com.ardverk.dht.io.process;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.io.MessageDispatcher;

abstract class AbstractProcess<T> implements AsyncProcess<T> {

    protected final MessageDispatcher messageDispatcher;
    
    public AbstractProcess(MessageDispatcher messageDispatcher) {
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        
        this.messageDispatcher = messageDispatcher;
    }
}
