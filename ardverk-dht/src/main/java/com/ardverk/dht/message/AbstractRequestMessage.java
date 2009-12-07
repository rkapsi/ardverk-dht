package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {

    public AbstractRequestMessage(
            MessageId messageId, Contact source, 
            Contact destination, long time) {
        super(messageId, source, destination, time);
    }
}
