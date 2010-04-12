package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

/**
 * 
 */
public interface MessageCallback {

    /**
     * 
     */
    public void handleResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException;
    
    /**
     * 
     */
    public void handleTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException;
    
    /**
     * 
     */
    public void handleException(RequestMessage request, Throwable exception);
}
