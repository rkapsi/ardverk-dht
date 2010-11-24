package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;
import org.ardverk.concurrent.ValueReference;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkValueFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.DefaultBootstrapEntity;
import com.ardverk.dht.entity.DefaultRefreshEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.RefreshEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;

class BootstrapManager {

    private final DHT dht;
    
    private final RouteTable routeTable;
    
    public BootstrapManager(DHT dht, RouteTable routeTable) {
        this.dht = dht;
        this.routeTable = routeTable;
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            String host, int port, BootstrapConfig config) {
        return bootstrap(queueKey, new InetSocketAddress(host, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            InetAddress address, int port, BootstrapConfig config) {
        return bootstrap(queueKey, new InetSocketAddress(address, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            Contact contact, BootstrapConfig config) {
        return bootstrap(queueKey, contact.getContactAddress(), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            SocketAddress address, BootstrapConfig config) {
        
        Object lock = new Object();
        ArdverkFuture<PingEntity> pingFuture = dht.ping(
                queueKey, address, config.getPingConfig());
        
        synchronized (lock) {
            return bootstrap(lock, pingFuture, queueKey, config);
        }
    }
    
    private ArdverkFuture<BootstrapEntity> bootstrap(final Object lock, 
            final ArdverkFuture<PingEntity> pingFuture, 
            final QueueKey queueKey, final BootstrapConfig config) {
        
        // Make sure we're holding the lock!
        assert (Thread.holdsLock(lock));
        
        AsyncProcess<BootstrapEntity> process = NopAsyncProcess.create();
        final ArdverkFuture<BootstrapEntity> userFuture 
            = dht.submit(queueKey, process, config);
        
        final ValueReference<AsyncFuture<NodeEntity>> lookupFutureRef
            = new ValueReference<AsyncFuture<NodeEntity>>();
        
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
                KUID localhostId = localhost.getContactId();
                AsyncFuture<NodeEntity> lookupFuture 
                    = lookupFutureRef.make(
                        dht.lookup(queueKey, localhostId, 
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
                    
                    private void handleNodeEntity(NodeEntity nodeEntity) {
                        userFuture.setValue(new DefaultBootstrapEntity(
                                pingEntity, nodeEntity));
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
                }
            }
        });
        
        return userFuture;
    }
    
    @SuppressWarnings("unchecked")
    public ArdverkFuture<RefreshEntity> refresh(QueueKey queueKey, RefreshConfig config) {
        
        long startTime = System.currentTimeMillis();
        
        List<ArdverkFuture<PingEntity>> pingFutures 
            = new ArrayList<ArdverkFuture<PingEntity>>();
        
        List<ArdverkFuture<NodeEntity>> lookupFutures 
            = new ArrayList<ArdverkFuture<NodeEntity>>();
        
        synchronized (routeTable) {
            int pingCount = config.getPingCount();
            KUID localhostId = dht.getLocalhost().getContactId();

            if (0 < pingCount) {
                PingConfig pingConfig = config.getPingConfig();
                long contactTimeout = config.getContactTimeoutInMillis();
                
                Contact[] contacts = routeTable.select(localhostId, pingCount);
                for (Contact contact : contacts) {
                    if (contact.isTimeout(contactTimeout, TimeUnit.MILLISECONDS)) {
                        ArdverkFuture<PingEntity> future = dht.ping(
                                queueKey, contact, pingConfig);
                        pingFutures.add(future);
                    }
                }
            }
            
            LookupConfig lookupConfig = config.getLookupConfig();
            long bucketTimeout = config.getBucketTimeoutInMillis();
            KUID[] bucketIds = routeTable.refresh(
                    bucketTimeout, TimeUnit.MILLISECONDS);
            
            if (bucketIds != null) {
                for (KUID bucketId : bucketIds) {
                    ArdverkFuture<NodeEntity> future = dht.lookup(
                            queueKey, bucketId, lookupConfig);
                    lookupFutures.add(future);
                }
            }
        }
        
        ArdverkFuture<PingEntity>[] pings = pingFutures.toArray(new ArdverkFuture[0]);
        ArdverkFuture<NodeEntity>[] lookups = lookupFutures.toArray(new ArdverkFuture[0]);
        
        return new RefreshFuture(startTime, pings, lookups);
    }
    
    private static class RefreshFuture extends ArdverkValueFuture<RefreshEntity> {
        
        private final AtomicInteger coutdown = new AtomicInteger();
        
        private final long startTime;
        
        private final ArdverkFuture<PingEntity>[] pingFutures;
        
        private final ArdverkFuture<NodeEntity>[] lookupFutures;
        
        @SuppressWarnings("unchecked")
        public RefreshFuture(long startTime, 
                ArdverkFuture<PingEntity>[] pingFutures, 
                ArdverkFuture<NodeEntity>[] lookupFutures) {
            this.startTime = startTime;
            this.pingFutures = pingFutures;
            this.lookupFutures = lookupFutures;
            
            coutdown.set(pingFutures.length + lookupFutures.length);
            
            AsyncFutureListener<?> listener 
                    = new AsyncFutureListener<Object>() {
                @Override
                public void operationComplete(AsyncFuture<Object> future) {
                    coutdown();
                }
            };
            
            for (ArdverkFuture<PingEntity> future : pingFutures) {
                future.addAsyncFutureListener(
                        (AsyncFutureListener<PingEntity>)listener);
            }
            
            for (ArdverkFuture<NodeEntity> future : lookupFutures) {
                future.addAsyncFutureListener(
                        (AsyncFutureListener<NodeEntity>)listener);
            }
        }
        
        @Override
        protected void done() {
            super.done();
            
            FutureUtils.cancelAll(pingFutures, true);
            FutureUtils.cancelAll(lookupFutures, true);
        }
        
        private void coutdown() {
            if (coutdown.decrementAndGet() == 0) {
                long time = System.currentTimeMillis() - startTime;
                setValue(new DefaultRefreshEntity(pingFutures, lookupFutures, 
                        time, TimeUnit.MILLISECONDS));
            }
        }
    }
}
