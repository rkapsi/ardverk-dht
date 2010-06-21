package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Condition status;
    
    public DefaultStoreResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Condition status) {
        super(messageId, contact, address);
        
        this.status = Arguments.notNull(status, "status");
    }
    
    @Override
    public Condition getStatus() {
        return status;
    }
}