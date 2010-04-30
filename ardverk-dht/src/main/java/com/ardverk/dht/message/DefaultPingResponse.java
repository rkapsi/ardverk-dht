package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(MessageId messageId, Contact2 contact) {
        super(messageId, contact);
    }
}
