package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingRequest extends AbstractRequestMessage 
        implements PingRequest {

    public DefaultPingRequest(
            MessageId messageId, Contact source, 
            SocketAddress address) {
        super(messageId, source, address);
    }
}
