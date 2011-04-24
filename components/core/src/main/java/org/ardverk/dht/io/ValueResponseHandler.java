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
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.dht.storage.ValueTuple;


/**
 * The {@link ValueResponseHandler} manages a {@link MessageType#FIND_VALUE} 
 * lookup process.
 */
public class ValueResponseHandler extends LookupResponseHandler<ValueEntity> {
    
    private final FixedSizeArrayList<Resource> resources;
    
    private final ResourceId resourceId;
    
    public ValueResponseHandler(MessageDispatcher messageDispatcher,
            Contact[] contacts, RouteTable routeTable, 
            ResourceId resourceId, GetConfig config) {
        super(messageDispatcher, contacts, routeTable, 
                resourceId.getId(), config);
        
        resources = new FixedSizeArrayList<Resource>(config.getR());
        this.resourceId = resourceId;
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
        
        Resource resource = response.getResource();
        resources.add(resource);
        
        if (resources.isFull()) {
            Outcome outcome = createOutcome();
            ValueTuple[] values = resources.toArray(new ValueTuple[0]);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
    
    @Override
    protected void lookup(Contact dst, KUID lookupId, 
            long timeout, TimeUnit unit) throws IOException {
        
        assert (lookupId.equals(resourceId.getId()));
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ValueRequest message = factory.createValueRequest(dst, resourceId);
        
        send(dst, message, timeout, unit);
    }
    
    @Override
    protected void complete(Outcome outcome) {
        
        if (resources.isEmpty()) {
            setException(new NoSuchValueException(outcome));
        } else {
            ValueTuple[] values = resources.toArray(new ValueTuple[0]);
            setValue(new DefaultValueEntity(outcome, values));
        }
    }
}