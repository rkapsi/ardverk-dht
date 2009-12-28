package com.ardverk.dht.message;

import java.net.SocketAddress;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Status;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Status status;
    
    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            Contact destination, Status status) {
        super(messageId, contact, destination);
        
        if (status == null) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
    }
    
    public DefaultStoreResponse(
            MessageId messageId, Contact contact, 
            SocketAddress address, Status status) {
        super(messageId, contact, address);
        
        if (status == null) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
    }
    
    public Status getStatus() {
        return status;
    }
}