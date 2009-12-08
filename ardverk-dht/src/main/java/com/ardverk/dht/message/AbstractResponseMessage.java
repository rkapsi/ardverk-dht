package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( 
            MessageId messageId, Contact source, 
            SocketAddress address) {
        super(messageId, source, address);
    }
}
