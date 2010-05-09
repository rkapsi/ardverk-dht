package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.NullArgumentException;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Condition status;
    
    public DefaultStoreResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Condition status) {
        super(messageId, contact, address);
        
        if (status == null) {
            throw new NullArgumentException("status");
        }
        
        this.status = status;
    }
    
    @Override
    public Condition getStatus() {
        return status;
    }
}