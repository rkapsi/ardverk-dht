package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.DefaultNodeStoreEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.NodeStoreEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.routing.Contact;

public class StoreManager {

    private final DHT dht;
    
    public StoreManager(DHT dht) {
        this.dht = dht;
    }
    
    public ArdverkFuture<StoreEntity> store(Contact[] dst, KUID key, 
            Value value, StoreConfig config) {
        return null;
    }
    
    public ArdverkFuture<NodeStoreEntity> store(KUID key, Value value, 
            StoreConfig config) {
        return null;
    }
    
    public ArdverkFuture<NodeStoreEntity> put(final KUID key, final Value value,
            final StoreConfig config) {
        
        final Object lock = new Object();
        synchronized (lock) {
            
            final ArdverkFuture<NodeEntity> lookupFuture 
                = dht.lookup(key, config.getLookupConfig());
            
            final AtomicReference<ArdverkFuture<StoreEntity>> storeFutureRef 
                = new AtomicReference<ArdverkFuture<StoreEntity>>();
            
            long combinedTimeout = config.getCombinedTimeout(TimeUnit.MILLISECONDS);
            AsyncProcess<NodeStoreEntity> process = NopAsyncProcess.create();
            final ArdverkFuture<NodeStoreEntity> userFuture 
                = dht.submit(process, combinedTimeout, TimeUnit.MILLISECONDS);
            userFuture.addAsyncFutureListener(new AsyncFutureListener<NodeStoreEntity>() {
                @Override
                public void operationComplete(AsyncFuture<NodeStoreEntity> future) {
                    synchronized (lock) {
                        FutureUtils.cancel(lookupFuture, true);
                        FutureUtils.cancel(storeFutureRef, true);
                    }
                }
            });
            
            lookupFuture.addAsyncFutureListener(new AsyncFutureListener<NodeEntity>() {
                @Override
                public void operationComplete(AsyncFuture<NodeEntity> future) {
                    synchronized (lock) {
                        try {
                            if (!future.isCancelled()) {
                                handleValue(future.get());
                            } else {
                                handleCancelled();
                            }
                        } catch (Throwable t) {
                            handleException(t);
                        }
                    }
                }
                
                private void handleValue(final NodeEntity nodeEntity) {
                    // TODO: Fix the nulls
                    AsyncProcess<StoreEntity> process 
                        = new StoreResponseHandler(null, nodeEntity, null);
                    ArdverkFuture<StoreEntity> storeFuture = dht.submit(process, config);
                    storeFuture.addAsyncFutureListener(new AsyncFutureListener<StoreEntity>() {
                        @Override
                        public void operationComplete(AsyncFuture<StoreEntity> future) {
                            synchronized (lock) {
                                try {
                                    if (!future.isCancelled()) {
                                        handleValue(future.get());
                                    } else {
                                        handleCancelled();
                                    }
                                } catch (Throwable t) {
                                    handleException(t);
                                }
                            }
                        }
                        
                        private void handleValue(StoreEntity storeEntity) {
                            long time = nodeEntity.getTimeInMillis() 
                                    + storeEntity.getTimeInMillis();
                            
                            userFuture.setValue(new DefaultNodeStoreEntity(
                                    nodeEntity, time, TimeUnit.MILLISECONDS));
                        }
                    });
                    
                    storeFutureRef.set(storeFuture);
                }
                
                private void handleCancelled() {
                    userFuture.cancel(true);
                }
                
                private void handleException(Throwable t) {
                    userFuture.setException(t);
                }
            });
            
            return userFuture;
        }
    }
    
    public ArdverkFuture<StoreEntity> put(Contact[] dst, KUID key, 
            Value value, StoreConfig config) {
        return null;
    }
}
