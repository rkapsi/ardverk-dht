package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.entity.Entity;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.utils.Checkable;

public interface ResponseHandler<V extends Entity> 
        extends MessageCallback, Checkable, AsyncProcess<V> {

    public void send(RequestMessage message, long timeout, TimeUnit unit)
            throws IOException;
}