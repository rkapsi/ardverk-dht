package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {

    public AbstractRequestMessage(
            MessageId messageId, Contact source, 
            SocketAddress address) {
        super(messageId, source, address);
    }
}
