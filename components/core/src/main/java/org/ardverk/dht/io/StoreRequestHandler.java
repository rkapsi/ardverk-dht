/*
 * Copyright 2009-2012 Roger Kapsi
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.ResponseMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Datastore;


/**
 * The {@link StoreRequestHandler} is called for all {@link StoreRequest} 
 * ({@link MessageType#STORE}) messages.
 */
@Singleton
public class StoreRequestHandler extends AbstractRequestHandler {
    
    private final Datastore datastore;
    
    @Inject
    public StoreRequestHandler(
            Provider<MessageDispatcher> messageDispatcher,
            Datastore datastore) {
        super(messageDispatcher);
        
        this.datastore = datastore;
    }

    private Value store(StoreRequest request) {
        Contact src = request.getContact();
        Key key = request.getKey();
        Value value = request.getValue();
        return datastore.store(src, key, value);
    }
    
    @Override
    public ResponseMessage handleRequest(RequestMessage message) throws IOException {
        StoreRequest request = (StoreRequest)message;
        Value value = store(request);
        
        MessageFactory factory = getMessageFactory();
        return factory.createStoreResponse(request, value);
    }
}