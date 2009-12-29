package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;

public class PingRequestHandler extends AbstractRequestHandler {
    
    public PingRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        PingRequest request = (PingRequest)message;
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        PingResponse response = factory.createPingResponse(request);
        messageDispatcher.send(response);
    }
}
