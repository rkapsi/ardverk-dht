/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.FixedSizeArrayList;

import com.ardverk.dht.KUID;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.entity.DefaultValueEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.MessageType;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.ValueTuple;

/**
 * The {@link ValueResponseHandler} manages a {@link MessageType#FIND_VALUE} 
 * lookup process.
 */
public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private final FixedSizeArrayList<ValueTuple> tuples;
    
    private final KUID valueId;
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            Contact[] contacts, RouteTable routeTable, KUID valueId, GetConfig config) {
        super(messageDispatcher, contacts, routeTable, valueId, config);
        
        tuples = new FixedSizeArrayList<ValueTuple>(config.getR());
        this.valueId = valueId;
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
        
        assert (lookupId.equals(valueId));
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, valueId);
        
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