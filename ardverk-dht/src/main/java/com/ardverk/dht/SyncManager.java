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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.utils.ArrayUtils;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkValueFuture;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.DefaultSyncEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.Localhost;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.ValueTuple;
import com.ardverk.dht.utils.ContactKey;

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
        
    public ArdverkFuture<SyncEntity> sync(final SyncConfig syncConfig) {
        
        final Object lock = new Object();
        
        synchronized (lock) {
            
            final long startTime = System.currentTimeMillis();
            
            final Map<ContactKey, ArdverkFuture<PingEntity>> futures 
                = new HashMap<ContactKey, ArdverkFuture<PingEntity>>();
            
            final List<ArdverkFuture<StoreEntity>> storeFutures 
                = new ArrayList<ArdverkFuture<StoreEntity>>();
            
            Localhost localhost = routeTable.getLocalhost();
            PingConfig pingConfig = syncConfig.getPingConfig();
            
            final ArdverkFuture<SyncEntity> userFuture 
                = new ArdverkValueFuture<SyncEntity>();
            
            // The number of PINGs we've sent
            final AtomicInteger pingCounter = new AtomicInteger();
            
            // The number of STOREs we've sent
            final AtomicInteger storeCounter = new AtomicInteger();
            
            for (final ValueTuple tuple : database.values()) {
                KUID valueId = tuple.getId();
                Contact[] contacts = routeTable.select(valueId);
                
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
                        
                        ArdverkFuture<StoreEntity> storeFuture 
                            = store(tuple, syncConfig.getStoreConfig());
                        storeCounter.incrementAndGet();
                        storeFuture.addAsyncFutureListener(
                                new AsyncFutureListener<StoreEntity>() {
                            @Override
                            public void operationComplete(AsyncFuture<StoreEntity> future) {
                                synchronized (lock) {
                                    try {
                                        storeFutures.add((ArdverkFuture<StoreEntity>)future);
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
                        long time = System.currentTimeMillis() - startTime;
                        
                        @SuppressWarnings("unchecked")
                        ArdverkFuture<StoreEntity>[] futures 
                            = storeFutures.toArray(new ArdverkFuture[0]);
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
            
            if (pingCounter.get() == 0 
                    && storeCounter.get() == 0) {
                long time = System.currentTimeMillis() - startTime;
                userFuture.setValue(new DefaultSyncEntity(time, TimeUnit.MILLISECONDS));
            }
            
            return userFuture;
        }
    }
    
    private ArdverkFuture<StoreEntity> store(ValueTuple tuple, StoreConfig storeConfig) {
        Localhost localhost = routeTable.getLocalhost();
        Contact[] contacts = routeTable.select(localhost.getId());
        assert (localhost.equals(contacts[0]));
        
        // TODO: What's better?
        //Contact[] dst = new Contact[contacts.length-1];
        //System.arraycopy(contacts, 1, dst, 0, dst.length);
        //return storeManager.store(dst, tuple, storeConfig);
        
        return storeManager.store(contacts, tuple, storeConfig);
    }
    
    private PingFuture ping(Map<ContactKey, ArdverkFuture<PingEntity>> futures, 
            Contact[] contacts, int toIndex, PingConfig pingConfig) {
        
        List<ArdverkFuture<PingEntity>> pingFutures 
            = new ArrayList<ArdverkFuture<PingEntity>>(toIndex);
        
        for (int i = 0; i < toIndex; i++) {
            Contact contact = contacts[i];
            final ContactKey key = new ContactKey(contact);
            
            ArdverkFuture<PingEntity> future = futures.get(key);
            if (future == null) {
                future = pingManager.ping(contact, pingConfig);
                futures.put(key, future);
            }
            
            pingFutures.add(future);
        }
        
        return new PingFuture(pingFutures, toIndex);
    }
    
    private static class PingFuture extends ArdverkValueFuture<Sync> {
        
        private final AtomicInteger countdown = new AtomicInteger();
        
        private final List<ArdverkFuture<PingEntity>> futures;
        
        private final int index;
        
        private PingFuture(List<ArdverkFuture<PingEntity>> futures, int index) {
            this.futures = futures;
            this.index = index;
            
            countdown.set(futures.size());
            
            // It's possible that countdown is 0!
            if (0 < countdown.get()) {
                AsyncFutureListener<PingEntity> listener 
                        = new AsyncFutureListener<PingEntity>() {
                    @Override
                    public void operationComplete(AsyncFuture<PingEntity> future) {
                        coutdown();
                    }
                };
                
                for (ArdverkFuture<PingEntity> future : futures) {
                    future.addAsyncFutureListener(listener);
                }
            } else {
                complete();
            }
        }
        
        @Override
        protected void done() {
            super.done();
            
            FutureUtils.cancelAll(futures, true);
        }
        
        private void coutdown() {
            if (countdown.decrementAndGet() == 0) {
                complete();
            }
        }
        
        private void complete() {
            boolean store = true;
            for (ArdverkFuture<PingEntity> future : futures) {
                if (!future.isCompletedAbnormally()) {
                    store = false;
                    break;
                }
            }
            
            setValue((store || index == 0) ? Sync.STORE_VALUE : Sync.SKIP_VALUE);
        }
    }
}