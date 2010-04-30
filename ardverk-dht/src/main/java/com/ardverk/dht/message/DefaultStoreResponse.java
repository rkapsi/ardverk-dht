package com.ardverk.dht.message;

import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Condition status;
    
    public DefaultStoreResponse(MessageId messageId, 
            Contact2 contact, Condition status) {
        super(messageId, contact);
        
        if (status == null) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
    }
    
    public Condition getStatus() {
        return status;
    }
}