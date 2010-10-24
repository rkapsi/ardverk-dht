package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncAtomicReference;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.DefaultNodeStoreEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.StoreResponseHandler;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.DefaultValueTuple;
import com.ardverk.dht.storage.Value;
import com.ardverk.dht.storage.ValueTuple;

class StoreManager {

    private final DHT dht;
    
    private final MessageDispatcher messageDispatcher;
    
    public StoreManager(DHT dht, MessageDispatcher messageDispatcher) {
        this.dht = dht;
        this.messageDispatcher = messageDispatcher;
    }
    
    public ArdverkFuture<StoreEntity> put(final QueueKey queueKey, 
            final KUID key, Value value, final StoreConfig config) {
        
        Contact localhost = dht.getLocalhost();
        final ValueTuple valueTuple 
            = new DefaultValueTuple(localhost, key, value);
        
        final Object lock = new Object();
        synchronized (lock) {
            
            // Start the lookup for the given KUID
            final ArdverkFuture<NodeEntity> lookupFuture 
                = dht.lookup(queueKey, key, config.getLookupConfig());
            
            // This will get initialized once we've found the k-closest
            // Contacts to the given KUID
            final AsyncAtomicReference<ArdverkFuture<StoreEntity>> storeFutureRef 
                = new AsyncAtomicReference<ArdverkFuture<StoreEntity>>();
            
            // This is the ArdverkFuture we're going to return to the caller
            // of this method (in most cases the user).
            long combinedTimeout = config.getCombinedTimeout(TimeUnit.MILLISECONDS);
            AsyncProcess<StoreEntity> process = NopAsyncProcess.create();
            final ArdverkFuture<StoreEntity> userFuture 
                = dht.submit(QueueKey.DEFAULT, process, combinedTimeout, TimeUnit.MILLISECONDS);
            userFuture.addAsyncFutureListener(new AsyncFutureListener<StoreEntity>() {
                @Override
                public void operationComplete(AsyncFuture<StoreEntity> future) {
                    synchronized (lock) {
                        FutureUtils.cancel(lookupFuture, true);
                        FutureUtils.cancel(storeFutureRef, true);
                    }
                }
            });
            
            // Let's wait for the result of the FIND_NODE operation. On success
            // we're going to initialize the storeFutureRef and do the actual
            // STOREing.
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
                    AsyncProcess<StoreEntity> process 
                        = new StoreResponseHandler(messageDispatcher, 
                                nodeEntity, valueTuple);
                    
                    ArdverkFuture<StoreEntity> storeFuture 
                        = storeFutureRef.make(
                            dht.submit(queueKey, process, config));
                    
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
                            long time = nodeEntity.getTimeInMillis() 
                                    + storeEntity.getTimeInMillis();
                            
                            userFuture.setValue(new DefaultNodeStoreEntity(
                                    nodeEntity, time, TimeUnit.MILLISECONDS));
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
            
            return userFuture;
        }
    }
    
    public ArdverkFuture<StoreEntity> put(QueueKey queueKey, 
            Contact[] dst, KUID key, Value value, StoreConfig config) {
        return null;
    }
}
