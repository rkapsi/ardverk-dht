package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;

public class NodeRequestHandler extends RequestHandler {

    public NodeRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void handleRequest(RequestMessage request) throws IOException {
    }
}
