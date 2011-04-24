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
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTProcess;
import org.ardverk.dht.concurrent.NopProcess;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.entity.DefaultPutEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.StoreEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.NoSuchValueException;
import org.ardverk.dht.io.StoreResponseHandler;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.ByteArrayValue;
import org.ardverk.dht.storage.DefaultDescriptor;
import org.ardverk.dht.storage.DefaultValueTuple;
import org.ardverk.dht.storage.Descriptor;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.ResourceId;
import org.ardverk.dht.storage.Value;
import org.ardverk.dht.storage.ValueTuple;
import org.ardverk.lang.ExceptionUtils;
import org.ardverk.version.VectorClock;


/**
 * The {@link StoreManager} manages STORE operations.
 */
public class StoreManager {
    
    private final ArdverkDHT dht;
    
    private final RouteTable routeTable;
    
    private final MessageDispatcher messageDispatcher;
    
    StoreManager(ArdverkDHT dht, RouteTable routeTable, 
            MessageDispatcher messageDispatcher) {
        this.dht = dht;
        this.routeTable = routeTable;
        this.messageDispatcher = messageDispatcher;
    }
    
    public DHTFuture<PutEntity> remove(ResourceId resourceId, 
            VectorClock<KUID> clock, PutConfig config) {
        return put(resourceId, ByteArrayValue.EMPTY, clock, config);
    }
    
    public DHTFuture<PutEntity> remove(ValueTuple tuple, PutConfig config) {
        
        Descriptor descriptor = tuple.getDescriptor();
        ResourceId resourceId = descriptor.getResource();
        VectorClock<KUID> clock = descriptor.getVectorClock();
        
        return remove(resourceId, clock, config);
    }

    public DHTFuture<PutEntity> update(ValueTuple tuple, Value value,
            PutConfig config) {
        
        Descriptor descriptor = tuple.getDescriptor();
        ResourceId resourceId = descriptor.getResource();
        VectorClock<KUID> clock = descriptor.getVectorClock();
        
        return put(resourceId, value, clock, config);
    }
    
    public DHTFuture<PutEntity> put(final ResourceId resourceId, final Value value, 
            final VectorClock<KUID> clock, final PutConfig config) {
        
        final Object lock = new Object();
        synchronized (lock) {
            
            // This is the DHTFuture we're going to return to the caller
            // of this method (in most cases the user).
            DHTProcess<PutEntity> process = NopProcess.create();
            final DHTFuture<PutEntity> userFuture 
                = dht.submit(process, config);
            
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
                = dht.lookup(resourceId.getId(), 
                        config.getLookupConfig());
            
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
                
                private void handleNodeEntity(NodeEntity nodeEntity) {
                    StoreConfig store = config.getStoreConfig();
                    if (clock != null || store.isSloppy()) {
                        doStore(nodeEntity, clock);
                    } else {
                        doGetVectorClock(nodeEntity);
                    }
                }
                
                private void doGetVectorClock(final NodeEntity nodeEntity) {
                    Contact[] contacts = nodeEntity.getContacts();
                    DHTFuture<ValueEntity> clockFuture 
                        = clockFutureRef.make(clock(contacts, 
                                resourceId, config.getGetConfig()));
                    
                    clockFuture.addAsyncFutureListener(new AsyncFutureListener<ValueEntity>() {
                        @Override
                        public void operationComplete(AsyncFuture<ValueEntity> future) {
                            synchronized (lock) {
                                try {
                                    if (!future.isCancelled()) {
                                        handleVectorClock(future.get());
                                    } else {
                                        handleCancelled();
                                    }
                                } catch (Throwable t) {
                                    handleVectorClockException(t);
                                }
                            }
                        }
                        
                        private void handleVectorClock(ValueEntity entity) {
                            Descriptor descriptor = entity.getDescriptor();
                            VectorClock<KUID> clock = descriptor.getVectorClock();
                            doStore(nodeEntity, clock);
                        }
                        
                        private void handleVectorClockException(Throwable t) {
                            if (ExceptionUtils.isCausedBy(
                                    t, NoSuchValueException.class)) {
                                doStore(nodeEntity, clock);
                            } else {
                                handleException(t);
                            }
                        }
                    });
                }
                
                private void doStore(final NodeEntity nodeEntity, 
                        VectorClock<KUID> clock) {
                    
                    Contact[] contacts = nodeEntity.getContacts();
                    DHTFuture<StoreEntity> storeFuture 
                        = storeFutureRef.make(store(contacts, 
                                resourceId, clock, value, config.getStoreConfig()));
                    
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
                        FutureUtils.cancel(clockFutureRef, true);
                    }
                }
            });
            
            return userFuture;
        }
    }
    
    private DHTFuture<ValueEntity> clock(Contact[] src, 
            ResourceId resourceId, GetConfig config) {
        LookupManager lookupManager = dht.getLookupManager();
        return lookupManager.get(src, resourceId, config);
    }
    
    public DHTFuture<StoreEntity> store(Contact[] dst, 
            ResourceId resourceId, VectorClock<KUID> clock, 
            Value value, StoreConfig config) {
        
        Contact localhost = dht.getLocalhost();
        
        if (!config.isSloppy() && clock == null) {
            clock = VectorClock.create();
        }
        
        if (clock != null) {
            clock = clock.append(localhost.getId());
        }
        
        Descriptor descriptor = new DefaultDescriptor(
                localhost, resourceId, clock);
        
        ValueTuple valueTuple = new DefaultValueTuple(
                descriptor, value);
        
        return store(dst, valueTuple, config);
    }
    
    /**
     * Sends a STORE request to the given list of {@link Contact}s.
     * 
     * NOTE: It's being assumed the {@link Contact}s are already sorted by
     * their XOR distance to the given {@link KUID}.
     */
    public DHTFuture<StoreEntity> store(Contact[] dst, 
            Resource resource, StoreConfig config) {
        
        int k = routeTable.getK();
        
        DHTProcess<StoreEntity> process 
            = new StoreResponseHandler(messageDispatcher, 
                dst, k, resource, config);
        
        return dht.submit(process, config);
    }
}