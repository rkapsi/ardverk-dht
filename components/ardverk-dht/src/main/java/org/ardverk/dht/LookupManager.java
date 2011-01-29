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

package org.ardverk.dht;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.config.LookupConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.NodeResponseHandler;
import org.ardverk.dht.io.ValueResponseHandler;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;

/**
 * The {@link LookupManager} manages FIND_NODE and FIND_VALUE lookups.
 */
public class LookupManager {

    private final FutureService futureService;
    
    private final MessageDispatcher messageDispatcher;
    
    private final RouteTable routeTable;
    
    LookupManager(FutureService futureService, 
            MessageDispatcher messageDispatcher, 
            RouteTable routeTable) {
        this.futureService = futureService;
        this.messageDispatcher = messageDispatcher;
        this.routeTable = routeTable;
    }
    
    public DHTFuture<NodeEntity> lookup(KUID lookupId, LookupConfig config) {
        Contact[] contacts = routeTable.select(lookupId);
        return lookup(contacts, lookupId, config);
    }
    
    public DHTFuture<NodeEntity> lookup(Contact[] contacts, 
            KUID lookupId, LookupConfig config) {
        
        DHTProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, 
                    contacts, routeTable, lookupId, config);
        return futureService.submit(process, config);
    }
    
    public DHTFuture<ValueEntity> get(KUID key, GetConfig config) {
        Contact[] contacts = routeTable.select(key);
        return get(contacts, key, config);
    }
    
    public DHTFuture<ValueEntity> get(Contact[] contacts, 
            KUID key, GetConfig config) {
        DHTProcess<ValueEntity> process
            = new ValueResponseHandler(messageDispatcher, contacts, 
                    routeTable, key, config);
        return futureService.submit(process, config);
    }
}