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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncCompletionService;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.CountDown;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.config.StoreConfig;
import org.ardverk.dht.config.SyncConfig;
import org.ardverk.dht.entity.DefaultSyncEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.StoreEntity;
import org.ardverk.dht.entity.SyncEntity;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.Localhost;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.storage.Database;
import org.ardverk.dht.storage.Resource;
import org.ardverk.dht.storage.ValueTuple;
import org.ardverk.dht.utils.ContactKey;
import org.ardverk.lang.TimeStamp;
import org.ardverk.utils.ArrayUtils;


/**
 * The {@link SyncManager} manages the sync process.
 */
public class SyncManager {
    
    private static enum Sync {
        STORE_VALUE,
        SKIP_VALUE;
    }
    
    private final PingManager pingManager;
    
    private final StoreManager storeManager;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    SyncManager(PingManager pingManager, StoreManager storeManager, 
            RouteTable routeTable, Database database) {
        this.pingManager = pingManager;
        this.storeManager = storeManager;
        this.routeTable = routeTable;
        this.database = database;
    }
        
    public DHTFuture<SyncEntity> sync(final SyncConfig syncConfig) {
        
        final Object lock = new Object();
        
        synchronized (lock) {
            
            final TimeStamp creationTime = TimeStamp.now();
            
            final Map<ContactKey, DHTFuture<PingEntity>> futures 
                = new HashMap<ContactKey, DHTFuture<PingEntity>>();
            
            final List<DHTFuture<StoreEntity>> storeFutures 
                = new ArrayList<DHTFuture<StoreEntity>>();
            
            Localhost localhost = routeTable.getLocalhost();
            PingConfig pingConfig = syncConfig.getPingConfig();
            
            final DHTFuture<SyncEntity> userFuture 
                = new DHTValueFuture<SyncEntity>();
            
            // The number of PINGs we've sent
            final CountDown pingCounter = new CountDown();
            
            // The number of STOREs we've sent
            final CountDown storeCounter = new CountDown();
            
            for (Resource resource : database.values()) {
                
                final ValueTuple tuple = database.get(resource);
                
                KUID bucketId = resource.getId();
                Contact[] contacts = routeTable.select(bucketId);
                
                // Skip all values for which we're not in the k-closest.
                // This can happen in caching scenarios!
                int index = ArrayUtils.indexOf(localhost, contacts);
                if (index == -1) {
                    continue;
                }
                
                // This is a bit tricky: We're sending a PING to the Contacts
                // 0 through index. NOTE: The Contact at contacts[index] is the
                // localhost! The idea is that any of these Contacts is better
                // suited to do this sync operation because they're closer to
                // the value's KUID. We're therefore sending them PINGs to check
                // if they're alive. If they are we're simply skipping the value
                // and assume they'll take care of the value.
                //
                // SPECIAL CASE: Let index be 0 (i.e. we're the closest Contact).
                // In this case we're actually never sending any PINGs because
                // we're already the closest Contact and PingFuture is acting
                // just as a proxy.
                
                PingFuture pingFuture = ping(futures, contacts, index, pingConfig);
                pingCounter.incrementAndGet();
                
                pingFuture.addAsyncFutureListener(new AsyncFutureListener<Sync>() {
                    @Override
                    public void operationComplete(AsyncFuture<Sync> future) {
                        synchronized (lock) {
                            try {
                                process(future);
                            } catch (Throwable impossible) {
                                handleException(impossible);
                            }
                        }
                    }
                    
                    private void process(AsyncFuture<Sync> future) 
                            throws InterruptedException, ExecutionException {
                        try {
                            if (!future.isCancelled()) {
                                handleValue(future.get());
                            }
                        } finally {
                            countdown(pingCounter);
                        }
                    }
                    
                    private void handleValue(Sync operation) {
                        if (operation != Sync.STORE_VALUE) {
                            return;
                        }
                        
                        DHTFuture<StoreEntity> storeFuture 
                            = store(tuple, syncConfig.getStoreConfig());
                        storeCounter.incrementAndGet();
                        storeFuture.addAsyncFutureListener(
                                new AsyncFutureListener<StoreEntity>() {
                            @Override
                            public void operationComplete(AsyncFuture<StoreEntity> future) {
                                synchronized (lock) {
                                    try {
                                        storeFutures.add((DHTFuture<StoreEntity>)future);
                                    } finally {
                                        countdown(storeCounter);
                                    }
                                }
                            }
                        });
                    }
                    
                    private void handleException(Throwable t) {
                        userFuture.setException(t);
                    }
                    
                    private void countdown(AtomicInteger counter) {
                        assert (Thread.holdsLock(lock));
                        counter.decrementAndGet();
                        
                        if (pingCounter.get() == 0 
                                && storeCounter.get() == 0) {
                            complete();
                        }
                    }
                    
                    private void complete() {
                        long time = creationTime.getAgeInMillis();
                        
                        @SuppressWarnings("unchecked")
                        DHTFuture<StoreEntity>[] futures 
                            = storeFutures.toArray(new DHTFuture[0]);
                        userFuture.setValue(new DefaultSyncEntity(
                                futures, time, TimeUnit.MILLISECONDS));
                    }
                });
            }
            
            userFuture.addAsyncFutureListener(new AsyncFutureListener<SyncEntity>() {
                @Override
                public void operationComplete(AsyncFuture<SyncEntity> future) {
                    synchronized (lock) {
                        FutureUtils.cancelAll(futures.values(), true);
                    }
                }
            });
            
            if (pingCounter.countDown() 
                    && storeCounter.countDown()) {
                long time = creationTime.getAgeInMillis();
                userFuture.setValue(new DefaultSyncEntity(time, TimeUnit.MILLISECONDS));
            }
            
            return userFuture;
        }
    }
    
    private DHTFuture<StoreEntity> store(ValueTuple tuple, StoreConfig storeConfig) {
        Localhost localhost = routeTable.getLocalhost();
        Contact[] contacts = routeTable.select(localhost.getId());
        assert (localhost.equals(contacts[0]));
        
        // TODO: What's better?
        //Contact[] dst = new Contact[contacts.length-1];
        //System.arraycopy(contacts, 1, dst, 0, dst.length);
        //return storeManager.store(dst, tuple, storeConfig);
        
        return storeManager.store(contacts, tuple, storeConfig);
    }
    
    private PingFuture ping(Map<ContactKey, DHTFuture<PingEntity>> futures, 
            Contact[] contacts, int toIndex, PingConfig pingConfig) {
        
        List<DHTFuture<PingEntity>> pingFutures 
            = new ArrayList<DHTFuture<PingEntity>>(toIndex);
        
        for (int i = 0; i < toIndex; i++) {
            Contact contact = contacts[i];
            final ContactKey key = new ContactKey(contact);
            
            DHTFuture<PingEntity> future = futures.get(key);
            if (future == null) {
                future = pingManager.ping(contact, pingConfig);
                futures.put(key, future);
            }
            
            pingFutures.add(future);
        }
        
        return new PingFuture(pingFutures, toIndex);
    }
    
    private static class PingFuture extends DHTValueFuture<Sync> {
        
        private final List<DHTFuture<PingEntity>> futures;
        
        private final int index;
        
        private PingFuture(List<DHTFuture<PingEntity>> futures, int index) {
            this.futures = futures;
            this.index = index;
            
            AsyncFuture<Void> complete 
                = AsyncCompletionService.createVoid(futures);
            complete.addAsyncFutureListener(new AsyncFutureListener<Void>() {
                @Override
                public void operationComplete(AsyncFuture<Void> future) {
                    complete();
                }
            });
        }
        
        @Override
        protected void done() {
            super.done();
            
            FutureUtils.cancelAll(futures, true);
        }
        
        private void complete() {
            boolean store = true;
            for (DHTFuture<PingEntity> future : futures) {
                if (!future.isCompletedAbnormally()) {
                    store = false;
                    break;
                }
            }
            
            setValue((store || index == 0) ? Sync.STORE_VALUE : Sync.SKIP_VALUE);
        }
    }
}