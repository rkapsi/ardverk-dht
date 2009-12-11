package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;

public class StoreRequestHandler extends RequestHandler {

    public StoreRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void handleRequest(RequestMessage request) throws IOException {
    }
}
