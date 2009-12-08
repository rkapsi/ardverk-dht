package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;

public interface MessageCallback {

    public void handleResponse(ResponseMessage response) throws IOException;
    
    public void handleTimeout(RequestMessage request) throws IOException;
}
