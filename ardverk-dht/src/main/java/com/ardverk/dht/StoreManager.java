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

package com.ardverk.dht;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;
import org.ardverk.concurrent.ValueReference;
import org.ardverk.lang.Bytes;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.entity.DefaultPutEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.DefaultValueTuple;
import com.ardverk.dht.storage.ValueTuple;

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
        return put(key, Bytes.EMPTY, config);
    }
    
    public ArdverkFuture<PutEntity> put(final KUID key, final byte[] value, 
            final PutConfig config) {
        
        final Object lock = new Object();
        synchronized (lock) {
            
            // This is the ArdverkFuture we're going to return to the caller
            // of this method (in most cases the user).
            AsyncProcess<PutEntity> process = NopAsyncProcess.create();
            final ArdverkFuture<PutEntity> userFuture 
                = dht.submit(process, config);
            
            // This will get initialized once we've found the k-closest
            // Contacts to the given KUID
            final ValueReference<ArdverkFuture<StoreEntity>> storeFutureRef 
                = new ValueReference<ArdverkFuture<StoreEntity>>();
            
            // Start the lookup for the given KUID
            final ArdverkFuture<NodeEntity> lookupFuture 
                = dht.lookup(key, config.getLookupConfig());
            
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
                                key, value, config.getStoreConfig()));
                    
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
            KUID key, byte[] value, StoreConfig config) {
        
        Contact localhost = dht.getLocalhost();
        ValueTuple valueTuple = new DefaultValueTuple(
                localhost, key, value);
        
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
        
        AsyncProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, 
                dst, k, valueTuple, config);
        
        return dht.submit(process, config);
    }
}