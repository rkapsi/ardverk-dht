package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ardverk.collection.IdentityHashSet;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;
import org.ardverk.concurrent.ValueReference;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.DefaultBootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.Contact;

class BootstrapManager {

    private final DHT dht;
    
    public BootstrapManager(DHT dht) {
        this.dht = dht;
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            String host, int port, BootstrapConfig config) {
        return bootstrap(queueKey, new InetSocketAddress(host, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            InetAddress address, int port, BootstrapConfig config) {
        return bootstrap(queueKey, new InetSocketAddress(address, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(final QueueKey queueKey, 
            SocketAddress address, final BootstrapConfig config) {
        
        final Object lock = new Object();
        synchronized (lock) {
            
            AsyncProcess<BootstrapEntity> process = NopAsyncProcess.create();
            final ArdverkFuture<BootstrapEntity> userFuture 
                = dht.submit(queueKey, process, config);
            
            final AsyncFuture<PingEntity> pingFuture = dht.ping(
                    queueKey, address, config.getPingConfig());
            
            final ValueReference<AsyncFuture<NodeEntity>> lookupFutureRef
                = new ValueReference<AsyncFuture<NodeEntity>>();
            
            final Foo bla = new Foo();
            
            pingFuture.addAsyncFutureListener(new AsyncFutureListener<PingEntity>() {
                @Override
                public void operationComplete(AsyncFuture<PingEntity> future) {
                    synchronized (lock) {
                        try {
                            if (!future.isCancelled()) {
                                handlePingEntity(future.get());
                            } else {
                                handleCancelled();
                            }
                        } catch (Throwable t) {
                            handleException(t);
                        }
                    }
                }
                
                private void handlePingEntity(final PingEntity pingEntity) {
                    Contact localhost = dht.getLocalhost();
                    AsyncFuture<NodeEntity> lookupFuture 
                        = lookupFutureRef.make(
                            dht.lookup(queueKey, localhost.getContactId(), 
                                config.getLookupConfig()));
                    
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
                            /*doRefreshBuckets();
                            
                            long time = pingEntity.getTimeInMillis() 
                                + nodeEntity.getTimeInMillis();
                            
                            userFuture.setValue(new DefaultBootstrapEntity(
                                    time, TimeUnit.MILLISECONDS));*/
                            
                            KUID[] bucketIds = new KUID[0];
                            
                            if (!config.isRefreshBuckets() 
                                    || bucketIds == null 
                                    || bucketIds.length == 0) {
                                long time = pingEntity.getTimeInMillis() 
                                    + nodeEntity.getTimeInMillis();
                                
                                userFuture.setValue(new DefaultBootstrapEntity(
                                        time, TimeUnit.MILLISECONDS));
                                return;
                            }
                            
                            AsyncFutureListener<NodeEntity> listener 
                                    = new AsyncFutureListener<NodeEntity>() {
                                
                                private final long startTime = System.currentTimeMillis();
                                
                                @Override
                                public void operationComplete(
                                        AsyncFuture<NodeEntity> future) {
                                    
                                    if (bla.complete(future)) {
                                        long time = pingEntity.getTimeInMillis() 
                                            + nodeEntity.getTimeInMillis()
                                            + (System.currentTimeMillis()-startTime);
                                        
                                        userFuture.setValue(new DefaultBootstrapEntity(
                                                time, TimeUnit.MILLISECONDS));
                                    }
                                }
                            };
                            
                            for (KUID bucketId : bucketIds) {
                                AsyncFuture<NodeEntity> future = dht.lookup(
                                        queueKey, bucketId, config.getLookupConfig());
                                future.addAsyncFutureListener(listener);
                                bla.add(future);
                            }
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
            
            userFuture.addAsyncFutureListener(new AsyncFutureListener<BootstrapEntity>() {
                @Override
                public void operationComplete(AsyncFuture<BootstrapEntity> future) {
                    synchronized (lock) {
                        FutureUtils.cancel(pingFuture, true);
                        FutureUtils.cancel(lookupFutureRef, true);
                        FutureUtils.cancelAll(bla, true);
                    }
                }
            });
            return userFuture;
        }
    }
    
    private void doRefreshBuckets() {
        
    }
    
    private static class Foo implements Iterable<AsyncFuture<?>> {
        
        private final Set<AsyncFuture<?>> futures 
            = new IdentityHashSet<AsyncFuture<?>>();
        
        public void add(AsyncFuture<?> future) {
            futures.add(future);
        }
        
        public boolean complete(AsyncFuture<?> future) {
            futures.remove(future);
            return futures.isEmpty();
        }
        
        @Override
        public Iterator<AsyncFuture<?>> iterator() {
            return futures.iterator();
        }
    }
}
