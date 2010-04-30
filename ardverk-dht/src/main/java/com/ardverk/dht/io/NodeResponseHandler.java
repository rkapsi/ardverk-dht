package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultNodeEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;

public class NodeResponseHandler extends LookupResponseHandler<NodeEntity> {

    public NodeResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher, routeTable, key, alpha);
    }

    public NodeResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key) {
        super(messageDispatcher, routeTable, key);
    }
    
    @Override
    protected void lookup(Contact2 dst, KUID key, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeRequest message = factory.createNodeRequest(dst, key);
        
        long adaptiveTimeout = dst.getAdaptiveTimeout(timeout, unit);
        send(dst, message, adaptiveTimeout, unit);
    }

    @Override
    protected void complete(State state) {
        
        Contact2[] contacts = state.getContacts();
        
        if (contacts.length == 0) {
            setException(new IOException());                
        } else {
            setValue(new DefaultNodeEntity(state));
        }
    }
    
    @Override
    protected synchronized void processResponse0(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        Contact2 src = response.getContact();
        Contact2[] contacts = ((NodeResponse)response).getContacts();
        processContacts(src, contacts, time, unit);
    }
}
