package com.ardverk.dht.io;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public interface MessageCallback {

    public void handleResponse(ResponseMessage response) throws Exception;
    
    public void handleTimeout(RequestMessage request) throws Exception;
}
