package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {
    
    public AbstractRequestMessage(MessageId messageId, 
            Contact2 contact, Contact2 destination) {
        super(messageId, contact, destination);
    }

    public AbstractRequestMessage(
            MessageId messageId, Contact2 contact, 
            SocketAddress address) {
        super(messageId, contact, address);
    }
}
