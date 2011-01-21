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

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.ValueReference;
import org.ardverk.dht.concurrent.ArdverkFuture;
import org.ardverk.dht.concurrent.ArdverkProcess;
import org.ardverk.dht.concurrent.NopArdverkProcess;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.entity.DefaultPutEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.StoreEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.StoreResponseHandler;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.ByteArrayValue;
import org.ardverk.dht.storage.DefaultValueTuple;
import org.ardverk.dht.storage.Value;
import org.ardverk.dht.storage.ValueTuple;


/**
 * The {@link StoreManager} manages STORE operations.
 */
public class StoreManager {
    
    private final DHT dht;
    
    private final RouteTable routeTable;
    
    private final MessageDispatcher messageDispatcher;
    
    StoreManager(DHT dht, RouteTable routeTable, 
            MessageDispatcher messageDispatcher) {
        this.dht = dht;
        this.routeTable = routeTable;
        this.messageDispatcher = messageDispatcher;
    }
    
    public ArdverkFuture<PutEntity> remove(KUID key, PutConfig config) {
        return put(key, ByteArrayValue.EMPTY, config);
    }
    
    public ArdverkFuture<PutEntity> put(final KUID valueId, final Value value, 
            final PutConfig config) {
        
        final Object lock = new Object();
        synchronized (lock) {
            
            // This is the ArdverkFuture we're going to return to the caller
            // of this method (in most cases the user).
            ArdverkProcess<PutEntity> process = NopArdverkProcess.create();
            final ArdverkFuture<PutEntity> userFuture 
                = dht.submit(process, config);
            
            // This will get initialized once we've found the k-closest
            // Contacts to the given KUID
            final ValueReference<ArdverkFuture<StoreEntity>> storeFutureRef 
                = new ValueReference<ArdverkFuture<StoreEntity>>();
            
            // Start the lookup for the given KUID
            final ArdverkFuture<NodeEntity> lookupFuture 
                = dht.lookup(valueId, config.getLookupConfig());
            
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
                    ArdverkFuture<StoreEntity> storeFuture 
                        = storeFutureRef.make(store(contacts, 
                                valueId, value, config.getStoreConfig()));
                    
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
                            userFuture.setValue(new DefaultPutEntity(
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
                    }
                }
            });
            
            return userFuture;
        }
    }
    
    public ArdverkFuture<StoreEntity> store(Contact[] dst, 
            KUID valueId, Value value, StoreConfig config) {
        
        Contact localhost = dht.getLocalhost();
        ValueTuple valueTuple = new DefaultValueTuple(
                localhost, valueId, value);
        
        return store(dst, valueTuple, config);
    }
    
    /**
     * Sends a STORE request to the given list of {@link Contact}s.
     * 
     * NOTE: It's being assumed the {@link Contact}s are already sorted by
     * their XOR distance to the given {@link KUID}.
     */
    public ArdverkFuture<StoreEntity> store(Contact[] dst, 
            ValueTuple valueTuple, StoreConfig config) {
        
        int k = routeTable.getK();
        
        ArdverkProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, 
                dst, k, valueTuple, config);
        
        return dht.submit(process, config);
    }
}