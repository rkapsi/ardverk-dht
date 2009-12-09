package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;

public class DefaultPingResponse extends AbstractResponseMessage 
        implements PingResponse {

    public DefaultPingResponse(
            MessageId messageId, Contact contact, 
            Contact destination) {
        super(messageId, contact, destination);
    }
    
    public DefaultPingResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address) {
        super(messageId, contact, address);
    }
}
