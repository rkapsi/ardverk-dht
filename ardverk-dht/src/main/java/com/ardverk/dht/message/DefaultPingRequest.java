package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public class DefaultPingRequest extends AbstractRequestMessage 
        implements PingRequest {

    public DefaultPingRequest(MessageId messageId, Contact2 contact) {
        super(messageId, contact);
    }
}
