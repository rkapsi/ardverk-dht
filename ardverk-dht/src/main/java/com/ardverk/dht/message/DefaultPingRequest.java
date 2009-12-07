package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public class DefaultPingRequest extends AbstractRequestMessage 
        implements PingRequest {

    public DefaultPingRequest(
            MessageId messageId, Contact source, 
            Contact destination, long time) {
        super(messageId, source, destination, time);
    }
}
