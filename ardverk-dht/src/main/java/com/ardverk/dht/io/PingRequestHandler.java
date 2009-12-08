package com.ardverk.dht.io;

import com.ardverk.dht.message.Message;

public class PingRequestHandler extends RequestHandler {
    
    public PingRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    @Override
    public void handleMessage(Message message) throws Exception {
    }
}
