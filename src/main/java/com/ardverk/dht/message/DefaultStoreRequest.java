package com.ardverk.dht.message;

import java.net.SocketAddress;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.ValueTuple;

public class DefaultStoreRequest extends AbstractRequestMessage 
        implements StoreRequest {

    private final ValueTuple tuple;
    
    public DefaultStoreRequest(MessageId messageId, Contact contact, 
            SocketAddress address, ValueTuple tuple) {
        super(messageId, contact, address);
        
        this.tuple = Arguments.notNull(tuple, "tuple");
    }
    
    @Override
    public ValueTuple getValueTuple() {
        return tuple;
    }
}