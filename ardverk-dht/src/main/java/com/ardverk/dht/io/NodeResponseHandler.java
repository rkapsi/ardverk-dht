package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultNodeEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.LookupRequest;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
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
    protected void lookup(Contact dst, KUID key, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        LookupRequest message = factory.createNodeRequest(dst, key);
        messageDispatcher.send(this, message, timeout, unit);
    }

    @Override
    protected void complete(State state) {
        
        Contact[] contacts = state.getContacts();
        int hop = state.getHop();
        long time = state.getTimeInMillis();
        
        if (contacts.length == 0) {
            setException(new IOException());                
        } else {
            setValue(new DefaultNodeEntity(contacts, 
                    hop, time, TimeUnit.MILLISECONDS));
        }
    }
    
    @Override
    protected synchronized void processResponse0(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        Contact src = response.getContact();
        Contact[] contacts = ((NodeResponse)response).getContacts();
        processContacts(src, contacts, time, unit);
    }
}
