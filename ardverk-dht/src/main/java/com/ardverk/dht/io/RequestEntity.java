package com.ardverk.dht.io;

import java.net.SocketAddress;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.RequestMessage;

public class RequestEntity {

    private final KUID contactId;
    
    private final SocketAddress address;
    
    private final RequestMessage request;
    
    public RequestEntity(KUID contactId, 
            SocketAddress address, RequestMessage request) {
        
        this.contactId = contactId;
        this.address = address;
        this.request = request;
    }

    public KUID getContactId() {
        return contactId;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public RequestMessage getRequest() {
        return request;
    }
}
