package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.entity.DefaultNodeEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class NodeResponseHandler extends LookupResponseHandler<NodeEntity> {
    
    public NodeResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID lookupId, LookupConfig config) {
        super(messageDispatcher, routeTable, lookupId, config);
    }
    
    public NodeResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID lookupId, Contact[] contacts, LookupConfig config) {
        super(messageDispatcher, routeTable, lookupId, contacts, config);
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeRequest message = factory.createNodeRequest(dst, lookupId);
        
        send(dst, message, timeout, unit);
    }

    @Override
    protected void complete(Outcome outcome) {
        
        Contact[] contacts = outcome.getContacts();
        
        if (contacts.length == 0) {
            setException(new NoSuchNodeException(outcome));                
        } else {
            setValue(new DefaultNodeEntity(outcome));
        }
    }
    
    @Override
    protected synchronized void processResponse0(RequestEntity entity,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        Contact src = response.getContact();
        Contact[] contacts = ((NodeResponse)response).getContacts();
        processContacts(src, contacts, time, unit);
    }
}
