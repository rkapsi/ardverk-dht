package com.ardverk.dht.io;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.entity.Entity;
import com.ardverk.utils.Checkable;

public interface ResponseHandler<V extends Entity> 
        extends MessageCallback, Checkable, AsyncProcess<V> {

}