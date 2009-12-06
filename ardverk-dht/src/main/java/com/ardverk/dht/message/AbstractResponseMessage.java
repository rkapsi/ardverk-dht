package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( 
            MessageId messageId, Contact contact) {
        super(messageId, contact);
    }
}
