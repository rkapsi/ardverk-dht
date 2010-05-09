package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {
    
    public AbstractRequestMessage(MessageId messageId, 
            Contact contact, SocketAddress address) {
        super(messageId, contact, address);
    }
}
