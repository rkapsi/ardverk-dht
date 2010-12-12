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

import org.ardverk.lang.Arguments;
import org.ardverk.utils.ArrayUtils;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.DatabaseConfig;
import com.ardverk.dht.storage.DefaultCondition;
import com.ardverk.dht.storage.ValueTuple;

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

    @Override
    public void handleRequest(RequestMessage message) throws IOException {
        
        StoreRequest request = (StoreRequest)message;
        ValueTuple tuple = request.getValueTuple();
        Condition condition = null;
        
        DatabaseConfig config = database.getDatabaseConfig();
        if (config.isCheckBucket()) {
            KUID valueId = tuple.getId();
            Contact[] contacts = routeTable.select(valueId);
            Contact localhost = routeTable.getLocalhost();
            
            if (!ArrayUtils.contains(localhost, contacts)) {
                condition = DefaultCondition.FAILURE;
            } else {
                condition = database.store(tuple);
            }
        } else {
            condition = database.store(tuple);
        }
        
        MessageFactory factory = messageDispatcher.getMessageFactory();
        ResponseMessage response = factory.createStoreResponse(request, condition);
        send(request, response);
    }
}