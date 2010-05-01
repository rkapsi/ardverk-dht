package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( MessageId messageId, 
            Contact2 contact, SocketAddress address) {
        super(messageId, contact, address);
    }
}
