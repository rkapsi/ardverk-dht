package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class NodeResponseHandler extends LookupResponseHandler<NodeEntity> {
    
    public NodeResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key, LookupConfig config) {
        super(messageDispatcher, routeTable, key, config);
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        NodeRequest message = factory.createNodeRequest(dst, lookupId);
        
        send(dst, message, timeout, unit);
    }

    @Override
    protected void complete(NodeEntity nodeEntity) {
        
        Contact[] contacts = nodeEntity.getContacts();
        
        if (contacts.length == 0) {
            setException(new NodeNotFoundException(nodeEntity));                
        } else {
            setValue(nodeEntity);
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
    
    public static class NodeNotFoundException extends IOException {
        
        private static final long serialVersionUID = -2301202118771105303L;
        
        private final NodeEntity nodeEntity;
        
        private NodeNotFoundException(NodeEntity nodeEntity) {
            this.nodeEntity = nodeEntity;
        }

        public NodeEntity getNodeEntity() {
            return nodeEntity;
        }
    }
}
