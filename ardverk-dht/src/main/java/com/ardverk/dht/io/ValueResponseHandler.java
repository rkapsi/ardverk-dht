package com.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.LookupRequest;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private static final boolean EXHAUSTIVE = false;
    
    private final List<Object> values = new ArrayList<Object>();
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher, routeTable, key, alpha);
    }

    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key) {
        super(messageDispatcher, routeTable, key);
    }

    @Override
    protected synchronized void processResponse0(RequestMessage request,
            ResponseMessage response, long time, TimeUnit unit)
            throws IOException {
        
        if (response instanceof NodeResponse) {
            processNodeResponse((NodeResponse)response, time, unit);
        } else {
            processValueResponse((ValueResponse)response, time, unit);
        }
    }
    
    private synchronized void processNodeResponse(NodeResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        Contact src = response.getContact();
        Contact[] contacts = response.getContacts();
        processContacts(src, contacts, time, unit);
    }
    
    private synchronized void processValueResponse(ValueResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        values.add(response);
        
        if (!EXHAUSTIVE) {
            setValue(new DefaultValueEntity(time, unit));
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
        
        if (values.isEmpty()) {
            setException(new NoSuchValueException(
                    contacts, hop, time, unit));
        } else {
            setValue(new DefaultValueEntity(time, unit));
        }
    }
    
    public static class NoSuchValueException extends IOException {
        
        private static final long serialVersionUID = -2753236114164880872L;

        private final Contact[] contacts;
        
        private final int hop;
        
        private final long time;
        
        private final TimeUnit unit;
        
        private NoSuchValueException(Contact[] contacts, int hop, 
                long time, TimeUnit unit) {
            
            this.contacts = contacts;
            this.hop = hop;
            this.time = time;
            this.unit = unit;
        }

        public Contact[] getContacts() {
            return contacts;
        }

        public int getHop() {
            return hop;
        }

        public long getTime(TimeUnit unit) {
            return unit.convert(time, this.unit);
        }
        
        public long getTimeInMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }
    }
}
