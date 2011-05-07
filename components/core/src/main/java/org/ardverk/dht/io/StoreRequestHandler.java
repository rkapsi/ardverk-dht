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

import org.ardverk.dht.io.transport.Endpoint;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Database;


/**
 * The {@link StoreRequestHandler} is called for all {@link StoreRequest} 
 * ({@link MessageType#STORE}) messages.
 */
public class StoreRequestHandler extends AbstractRequestHandler {
    
    private final Database database;
    
    public StoreRequestHandler(
            MessageDispatcher messageDispatcher,
            Database database) {
        super(messageDispatcher);
        
        this.database = database;
    }

    private Value store(StoreRequest request) {
        Key key = request.getKey();
        Value value = request.getValue();
        return database.store(key, value);
    }
    
    public StoreResponse createResponse(StoreRequest request) throws IOException {
        Value value = store(request);
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        return factory.createStoreResponse(request, value);
    }
    
    @Override
    public void handleRequest(Endpoint endpoint, RequestMessage request) throws IOException {
        StoreResponse response = createResponse((StoreRequest)request);
        send(endpoint, request, response);
    }
}