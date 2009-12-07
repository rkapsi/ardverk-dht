package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( 
            MessageId messageId, Contact source, 
            Contact destination, long time) {
        super(messageId, source, destination, time);
    }
}
