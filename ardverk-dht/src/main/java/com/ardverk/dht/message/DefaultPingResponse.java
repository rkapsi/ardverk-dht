package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(
            MessageId messageId, Contact source, 
            Contact destination, long time) {
        super(messageId, source, destination, time);
    }
}
