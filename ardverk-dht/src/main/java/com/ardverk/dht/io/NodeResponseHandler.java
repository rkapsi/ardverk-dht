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
            Contact[] contacts, RouteTable routeTable, KUID lookupId, LookupConfig config) {
        super(messageDispatcher, contacts, routeTable, lookupId, config);
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