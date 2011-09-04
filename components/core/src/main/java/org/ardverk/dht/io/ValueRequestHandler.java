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

import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.ValueRequest;
import org.ardverk.dht.routing.Contact2;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Datastore;


/**
 * The {@link ValueRequestHandler} handles {@link ValueRequest} 
 * ({@link MessageType#FIND_VALUE}) messages. 
 */
public class ValueRequestHandler extends AbstractRequestHandler {

    private final RouteTable routeTable;
    
    private final Datastore datastore;
    
    public ValueRequestHandler(
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable, 
            Datastore datastore) {
        super(messageDispatcher);
        
        this.routeTable = routeTable;
        this.datastore = datastore;
    }
    
    @Override
    public ResponseMessage handleRequest(RequestMessage message) throws IOException {
        ValueRequest request = (ValueRequest)message;
        
        Contact2 src = request.getContact();
        Key key = request.getKey();
        Value value = datastore.get(src, key);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = null;
        
        if (value != null) {
            response = factory.createValueResponse(request, value);
        } else {
            Contact2[] contacts = routeTable.select(key.getId());
            response = factory.createNodeResponse(request, contacts);
        }
        
        return response;
    }
}