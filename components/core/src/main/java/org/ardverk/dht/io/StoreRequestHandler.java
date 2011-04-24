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

import org.ardverk.dht.KUID;
import org.ardverk.dht.io.transport.Endpoint;
import org.ardverk.dht.message.MessageFactory;
import org.ardverk.dht.message.MessageType;
import org.ardverk.dht.message.RequestMessage;
import org.ardverk.dht.message.StoreRequest;
import org.ardverk.dht.message.StoreResponse;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.DatabaseConfig;
import org.ardverk.dht.storage.DefaultStatus;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.dht.storage.Status;
import org.ardverk.lang.Arguments;
import org.ardverk.utils.ArrayUtils;


/**
 * The {@link StoreRequestHandler} is called for all {@link StoreRequest} 
 * ({@link MessageType#STORE}) messages.
 */
public class StoreRequestHandler extends AbstractRequestHandler {
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    public StoreRequestHandler(
            MessageDispatcher messageDispatcher,
            RouteTable routeTable, Database database) {
        super(messageDispatcher);
        
        this.routeTable = Arguments.notNull(routeTable, "routeTable");
        this.database = Arguments.notNull(database, "database");
    }

    public StoreResponse createResponse(StoreRequest request) {
        Resource resource = request.getResource();
        Status status = null;
        
        DatabaseConfig config = database.getDatabaseConfig();
        if (config.isCheckBucket()) {
            ResourceId resourceId = resource.getResourceId();
            
            KUID bucketId = resourceId.getId();
            Contact[] contacts = routeTable.select(bucketId);
            Contact localhost = routeTable.getLocalhost();
            
            if (!ArrayUtils.contains(localhost, contacts)) {
                status = DefaultStatus.FAILURE;
            } else {
                status = database.store(resource);
            }
        } else {
            status = database.store(resource);
        }
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        return factory.createStoreResponse(request, status);
    }
    
    @Override
    public void handleRequest(Endpoint endpoint, RequestMessage request) throws IOException {
        StoreResponse response = createResponse((StoreRequest)request);
        send(endpoint, request, response);
    }
}