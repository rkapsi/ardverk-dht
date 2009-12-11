package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.StoreRequest;

public class StoreRequestHandler extends RequestHandler {

    public StoreRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        StoreRequest request = (StoreRequest)message;
    }
}
