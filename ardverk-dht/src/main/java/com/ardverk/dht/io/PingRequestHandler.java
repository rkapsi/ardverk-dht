package com.ardverk.dht.io;

import com.ardverk.dht.message.PingRequest;

public class PingRequestHandler extends RequestHandler<PingRequest> {
    
    public PingRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    @Override
    public void handleMessage(PingRequest message) throws Exception {
    }
}
