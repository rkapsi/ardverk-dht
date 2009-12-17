package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.LookupRequest;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher, routeTable, key, alpha);
    }

    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key) {
        super(messageDispatcher, routeTable, key);
    }

    @Override
    protected void complete(Contact[] contacts, int hop, 
            long time, TimeUnit unit) {
        
        if (contacts.length == 0) {
            setException(new IOException());                
        } else {
            setValue(new DefaultValueEntity(time, TimeUnit.MILLISECONDS));
        }
    }
    
    @Override
    protected LookupRequest createLookupRequest(Contact dst, KUID key) {
        MessageFactory factory = messageDispatcher.getMessageFactory();
        return factory.createValueRequest(dst, key);
    }
}
