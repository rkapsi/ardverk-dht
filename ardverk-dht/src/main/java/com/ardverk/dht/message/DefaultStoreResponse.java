package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Condition status;
    
    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            Contact destination, Condition status) {
        super(messageId, contact, destination);
        
        if (status == null) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
    }
    
    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address, Condition status) {
        super(messageId, contact, address);
        
        if (status == null) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
    }
    
    public Condition getStatus() {
        return status;
    }
}