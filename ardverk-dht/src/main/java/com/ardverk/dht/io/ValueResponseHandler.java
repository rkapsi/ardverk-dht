package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.LookupRequest;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
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
    protected synchronized void processResponse(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        Contact src = response.getContact();

        if (response instanceof NodeResponse) {
            Contact[] contacts = ((NodeResponse)response).getContacts();
            processResponse(src, contacts, time, unit);
        } else {
            
        }
    }
    
    @Override
    protected void lookup(Contact dst, KUID key, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        LookupRequest message = factory.createValueRequest(dst, key);
        messageDispatcher.send(this, message, timeout, unit);
    }
    
    @Override
    protected void complete(Contact[] contacts, int hop, 
            long time, TimeUnit unit) {
        
        if (contacts.length == 0) {
            setException(new IOException());                
        } else {
            setValue(new DefaultValueEntity(time, unit));
        }
    }
}
