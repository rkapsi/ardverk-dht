package com.ardverk.dht.message;

import java.net.InetAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(MessageId messageId, Contact contact, 
            long time, InetAddress address) {
        super(messageId, contact, time, address);
    }
}
