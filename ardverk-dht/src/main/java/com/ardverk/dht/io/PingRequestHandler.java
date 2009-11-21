package com.ardverk.dht.io;

import com.ardverk.dht.message.PingRequest;

public class PingRequestHandler extends RequestHandler<PingRequest> {

    private final MessageDispatcher messageDispatcher;
    
    public PingRequestHandler(MessageDispatcher messageDispatcher) {
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        
        this.messageDispatcher = messageDispatcher;
    }
    
    @Override
    public void handleMessage(PingRequest message) throws Exception {
    }
}
