package com.ardverk.dht.message;

import java.net.InetAddress;

import com.ardverk.dht.routing.Contact;

public class AbstractResponseMessage extends AbstractMessage 
        implements ResponseMessage {

    public AbstractResponseMessage( 
            MessageId messageId, Contact contact, 
            long time, InetAddress address) {
        super(messageId, contact, time, address);
    }
}
