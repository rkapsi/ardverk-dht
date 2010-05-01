package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact2;

public class DefaultNodeRequest extends AbstractLookupRequest 
        implements NodeRequest {
    
    public DefaultNodeRequest(MessageId messageId, Contact2 contact, 
            SocketAddress address, KUID key) {
        super(messageId, contact, address, key);
    }
}