package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public abstract class AbstractRequestMessage extends AbstractMessage 
        implements RequestMessage {

    public AbstractRequestMessage(
            MessageId messageId, Contact contact) {
        super(messageId, contact);
    }
}
