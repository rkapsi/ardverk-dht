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

package org.ardverk.dht;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.config.ConfigProvider;
import org.ardverk.dht.config.ValueConfig;
import org.ardverk.dht.config.NodeConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.NodeResponseHandler;
import org.ardverk.dht.io.ValueResponseHandler;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.rsrc.Key;

/**
 * The {@link LookupManager} manages FIND_NODE and FIND_VALUE lookups.
 */
@Singleton
public class LookupManager {

  private final ConfigProvider configProvider;
  
  private final FutureManager futureManager;
  
  private final Provider<MessageDispatcher> messageDispatcher;
  
  private final RouteTable routeTable;
  
  @Inject
  LookupManager(ConfigProvider configProvider,
      RouteTable routeTable, 
      FutureManager futureManager, 
      Provider<MessageDispatcher> messageDispatcher) {
    
    this.configProvider = configProvider;
    this.futureManager = futureManager;
    this.messageDispatcher = messageDispatcher;
    this.routeTable = routeTable;
  }
  
  public DHTFuture<NodeEntity> lookup(KUID lookupId, NodeConfig... config) {
    Contact[] contacts = routeTable.select(lookupId);
    return lookup(contacts, lookupId, config);
  }
  
  public DHTFuture<NodeEntity> lookup(Contact[] contacts, 
      KUID lookupId, NodeConfig... config) {
    
    NodeConfig cfg = configProvider.get(config);
    
    DHTProcess<NodeEntity> process 
      = new NodeResponseHandler(messageDispatcher, 
          contacts, routeTable, lookupId, cfg);
    return futureManager.submit(process, cfg);
  }
  
  public DHTFuture<ValueEntity> get(Key key, ValueConfig... config) {
    Contact[] contacts = routeTable.select(key.getId());
    return get(contacts, key, config);
  }
  
  public DHTFuture<ValueEntity> get(Contact[] contacts, 
      Key key, ValueConfig... config) {
    
    ValueConfig cfg = configProvider.get(config);
    
    DHTProcess<ValueEntity> process
      = new ValueResponseHandler(messageDispatcher, contacts, 
          routeTable, key, cfg);
    return futureManager.submit(process, cfg);
  }
}