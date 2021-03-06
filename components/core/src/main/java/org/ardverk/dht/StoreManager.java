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

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.ValueReference;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.NopProcess;
import org.ardverk.dht.config.ConfigProvider;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.StoreEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.StoreResponseHandler;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;

/**
 * The {@link StoreManager} manages STORE operations.
 */
@Singleton
public class StoreManager {
  
  private final ConfigProvider configProvider;
  
  private final FutureManager futureManager;
  
  private final DiscoveryManager discoveryManager;
  
  private final RouteTable routeTable;
  
  private final Provider<MessageDispatcher> messageDispatcher;
  
  @Inject
  StoreManager(ConfigProvider configProvider, 
      RouteTable routeTable, 
      FutureManager futureManager, 
      DiscoveryManager discoveryManager,
      Provider<MessageDispatcher> messageDispatcher) {
    
    this.futureManager = futureManager;
    this.discoveryManager = discoveryManager;
    this.routeTable = routeTable;
    this.messageDispatcher = messageDispatcher;
    this.configProvider = configProvider;
  }
  
  public DHTFuture<PutEntity> put(final Key key, 
      final Value value, PutConfig config) {
    
    final PutConfig cfg = configProvider.get(config);
    
    int w = cfg.getStoreConfig().getW();
    if (w < 1) {
      throw new IllegalArgumentException("w=" + w);
    }
    
    if (w >= 2 && !value.isRepeatable()) {
      throw new IllegalArgumentException(
          "The value is not repeatable: w=" + w);
    }
    
    final Object lock = new Object();
    synchronized (lock) {
      
      // This is the DHTFuture we're going to return to the caller
      // of this method (in most cases the user).
      DHTProcess<PutEntity> process = NopProcess.create();
      final DHTFuture<PutEntity> userFuture 
        = futureManager.submit(process, cfg);
      
      // This will get initialized once we've found the k-closest
      // Contacts to the given KUID
      final ValueReference<DHTFuture<StoreEntity>> storeFutureRef 
        = new ValueReference<DHTFuture<StoreEntity>>();
      
      // This will get initialized once we've found the k-closest
      // Contacts to the given KUID
      final ValueReference<DHTFuture<ValueEntity>> clockFutureRef 
        = new ValueReference<DHTFuture<ValueEntity>>();
      
      // Start the lookup for the given KUID
      final DHTFuture<NodeEntity> lookupFuture 
        = discoveryManager.discover(key.getId(), 
            cfg.getLookupConfig());
      
      // Let's wait for the result of the FIND_NODE operation. On success we're 
      // going to initialize the storeFutureRef and do the actual STOREing.
      lookupFuture.addAsyncFutureListener(new AsyncFutureListener<NodeEntity>() {
        @Override
        public void operationComplete(AsyncFuture<NodeEntity> future) {
          synchronized (lock) {
            try {
              if (!future.isCancelled()) {
                handleNodeEntity(future.get());
              } else {
                handleCancelled();
              }
            } catch (Throwable t) {
              handleException(t);
            }
          }
        }
        
        private void handleNodeEntity(final NodeEntity nodeEntity) {
          Contact[] contacts = nodeEntity.getContacts();
          DHTFuture<StoreEntity> storeFuture 
            = storeFutureRef.make(store(contacts, 
                key, value, cfg.getStoreConfig()));
          
          storeFuture.addAsyncFutureListener(new AsyncFutureListener<StoreEntity>() {
            @Override
            public void operationComplete(AsyncFuture<StoreEntity> future) {
              synchronized (lock) {
                try {
                  if (!future.isCancelled()) {
                    handleStoreEntity(future.get());
                  } else {
                    handleCancelled();
                  }
                } catch (Throwable t) {
                  handleException(t);
                }
              }
            }
            
            private void handleStoreEntity(StoreEntity storeEntity) {
              userFuture.setValue(new PutEntity(
                  nodeEntity, storeEntity));
            }
          });
        }
        
        private void handleCancelled() {
          userFuture.cancel(true);
        }
        
        private void handleException(Throwable t) {
          userFuture.setException(t);
        }
      });
      
      userFuture.addAsyncFutureListener(new AsyncFutureListener<PutEntity>() {
        @Override
        public void operationComplete(AsyncFuture<PutEntity> future) {
          synchronized (lock) {
            FutureUtils.cancel(lookupFuture, true);
            FutureUtils.cancel(storeFutureRef, true);
            FutureUtils.cancel(clockFutureRef, true);
          }
        }
      });
      
      return userFuture;
    }
  }
  
  /**
   * Sends a STORE request to the given list of {@link Contact}s.
   * 
   * NOTE: It's being assumed the {@link Contact}s are already sorted by
   * their XOR distance to the given {@link KUID}.
   */
  public DHTFuture<StoreEntity> store(Contact[] dst, Key key, 
      Value value, StoreConfig config) {
    
    int k = routeTable.getK();
    DHTProcess<StoreEntity> process 
      = new StoreResponseHandler(messageDispatcher, 
        dst, k, key, value, config);
    
    return futureManager.submit(process, config);
  }
}