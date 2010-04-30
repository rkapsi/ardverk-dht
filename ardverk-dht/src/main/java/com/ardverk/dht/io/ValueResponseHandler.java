package com.ardverk.dht.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.RouteTable;

public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private static final boolean EXHAUSTIVE = false;
    
    private final List<byte[]> values = new ArrayList<byte[]>();
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key, int alpha) {
        super(messageDispatcher, routeTable, key, alpha);
    }

    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, KUID key) {
        super(messageDispatcher, routeTable, key);
    }

    @Override
    protected synchronized void processResponse0(RequestEntity request,
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
        
        Contact2 src = response.getContact();
        Contact2[] contacts = response.getContacts();
        processContacts(src, contacts, time, unit);
    }
    
    private synchronized void processValueResponse(ValueResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        values.add(response.getValue());
        
        if (!EXHAUSTIVE) {
            State state = getState();
            setValue(new DefaultValueEntity(state, response.getValue()));
        }
    }
    
    @Override
    protected void lookup(Contact2 dst, KUID key, 
            long timeout, TimeUnit unit) throws IOException {
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, key);
        
        long adaptiveTimeout = dst.getAdaptiveTimeout(timeout, unit);
        send(dst, message, adaptiveTimeout, unit);
    }
    
    @Override
    protected void complete(State state) {
        
        if (values.isEmpty()) {
            setException(new NoSuchValueException(state));
        } else {
            setValue(new DefaultValueEntity(state, values.get(0)));
        }
    }
    
    public static class NoSuchValueException extends IOException {
        
        private static final long serialVersionUID = -2753236114164880872L;

        private final State state;
        
        private NoSuchValueException(State state) {
            this.state = state;
        }

        public Contact2[] getContacts() {
            return state.getContacts();
        }

        public int getHop() {
            return state.getHop();
        }

        public long getTime(TimeUnit unit) {
            return state.getTime(unit);
        }
        
        public long getTimeInMillis() {
            return state.getTimeInMillis();
        }
    }
}
