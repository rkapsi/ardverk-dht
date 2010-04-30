package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( 
            MessageId messageId, Contact2 contact) {
        super(messageId, contact);
    }
}
