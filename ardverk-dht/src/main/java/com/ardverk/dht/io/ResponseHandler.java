package com.ardverk.dht.io;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.entity.Entity;

public interface ResponseHandler<V extends Entity> 
        extends MessageCallback, AsyncProcess<V> {

    /**
     * Returns {@code true} if the {@link ResponseHandler} is open.
     */
    public boolean isOpen();
}