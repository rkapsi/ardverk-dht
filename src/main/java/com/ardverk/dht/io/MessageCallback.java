package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.ResponseMessage;

/**
 * 
 */
public interface MessageCallback {

    /**
     * 
     */
    public void handleResponse(RequestEntity entity, ResponseMessage response, 
            long time, TimeUnit unit) throws IOException;
    
    /**
     * 
     */
    public void handleTimeout(RequestEntity entity, 
            long time, TimeUnit unit) throws IOException;
    
    /**
     * 
     */
    public void handleException(RequestEntity entity, Throwable exception);
}
