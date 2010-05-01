package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact2;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) {
        super(messageId, contact, address);
    }
}
