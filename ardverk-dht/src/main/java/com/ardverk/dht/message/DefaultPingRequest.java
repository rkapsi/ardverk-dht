package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingRequest extends AbstractRequestMessage 
        implements PingRequest {

    public DefaultPingRequest(MessageId messageId, 
            Contact contact, Contact destination) {
        super(messageId, contact, destination);
    }

    public DefaultPingRequest(MessageId messageId, 
            Contact contact, SocketAddress address) {
        super(messageId, contact, address);
    }
}
