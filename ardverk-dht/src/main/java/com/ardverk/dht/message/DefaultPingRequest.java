package com.ardverk.dht.message;

import java.net.InetAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingRequest extends AbstractRequestMessage 
        implements PingRequest {

    public DefaultPingRequest(MessageId messageId, Contact contact, 
            long time, InetAddress address) {
        super(messageId, contact, time, address);
    }
}
