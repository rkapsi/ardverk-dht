package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Database.Condition;

public class DefaultStoreResponse extends AbstractResponseMessage 
        implements StoreResponse {

    private final Condition condition;
    
    public DefaultStoreResponse(MessageId messageId, Contact contact, 
            SocketAddress address, Condition condition) {
        super(messageId, contact, address);
        
        this.condition = Arguments.notNull(condition, "condition");
    }
    
    @Override
    public Condition getCondition() {
        return condition;
    }
}