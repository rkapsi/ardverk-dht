package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.FixedSizeArrayList;
import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.ValueConfig;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Key;
import com.ardverk.dht.storage.ValueTuple;

public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private final FixedSizeArrayList<ValueTuple> tuples;
    
    private final Key key;
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            RouteTable routeTable, Key key, ValueConfig config) {
        super(messageDispatcher, routeTable, 
                key.getPrimaryKey(), config);
        
        tuples = new FixedSizeArrayList<ValueTuple>(config.getR());
        this.key = Arguments.notNull(key, "key");
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
        
        Contact src = response.getContact();
        Contact[] contacts = response.getContacts();
        processContacts(src, contacts, time, unit);
    }
    
    private synchronized void processValueResponse(ValueResponse response, 
            long time, TimeUnit unit) throws IOException {
        
        ValueTuple tuple = response.getValueTuple();
        tuples.add(tuple);
        
        if (tuples.isFull()) {
            Outcome outcome = createOutcome();
            ValueTuple[] values = tuples.toArray(new ValueTuple[0]);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        assert (lookupId.equals(key.getPrimaryKey()));
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, key);
        
        send(dst, message, timeout, unit);
    }
    
    @Override
    protected void complete(Outcome outcome) {
        
        if (tuples.isEmpty()) {
            setException(new NoSuchValueException(outcome));
        } else {
            ValueTuple[] values = tuples.toArray(new ValueTuple[0]);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
}
