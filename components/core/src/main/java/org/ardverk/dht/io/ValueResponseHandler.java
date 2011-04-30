/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.collection.FixedSizeArrayList;
import org.ardverk.dht.KUID;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.entity.DefaultValueEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.NodeResponse;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.message.ValueResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Key;


/**
 * The {@link ValueResponseHandler} manages a {@link MessageType#FIND_VALUE} 
 * lookup process.
 */
public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private final FixedSizeArrayList<ValueResponse> responses;
    
    private final Key key;
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            Contact[] contacts, RouteTable routeTable, 
            Key key, GetConfig config) {
        super(messageDispatcher, contacts, routeTable, 
                key.getId(), config);
        
        responses = new FixedSizeArrayList<ValueResponse>(config.getR());
        this.key = key;
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
        
        responses.add(response);
        
        if (responses.isFull()) {
            Outcome outcome = createOutcome();
            ValueResponse[] values = CollectionUtils.toArray(responses, ValueResponse.class);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        assert (lookupId.equals(key.getId()));
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, key);
        
        send(dst, message, timeout, unit);
    }
    
    @Override
    protected void complete(Outcome outcome) {
        
        if (responses.isEmpty()) {
            setException(new NoSuchValueException(outcome));
        } else {
            ValueResponse[] values = CollectionUtils.toArray(responses, ValueResponse.class);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
}