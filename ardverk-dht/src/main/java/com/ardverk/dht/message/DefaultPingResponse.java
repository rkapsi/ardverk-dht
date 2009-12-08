package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(
            MessageId messageId, Contact source, 
            SocketAddress address) {
        super(messageId, source, address);
    }
}
