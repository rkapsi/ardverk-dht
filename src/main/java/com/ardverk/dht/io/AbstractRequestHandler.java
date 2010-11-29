package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestHandler 
        extends AbstractMessageHandler implements RequestHandler {

    public AbstractRequestHandler(MessageDispatcher messageDispatcher) {
        super(messageDispatcher);
    }
    
    public void send(RequestMessage request, 
            ResponseMessage message) throws IOException {
        messageDispatcher.send(request.getContact(), message);
    }
    
    public void send(Contact dst, ResponseMessage message) throws IOException {
        messageDispatcher.send(dst, message);
    }
}
