package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;
import org.ardverk.concurrent.ValueReference;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.DefaultBootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
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
    
    public ArdverkFuture<?>[] refresh(QueueKey queueKey, RefreshConfig config) {
        
        List<ArdverkFuture<?>> futures 
            = new ArrayList<ArdverkFuture<?>>();
        
        synchronized (routeTable) {
            int pingCount = config.getPingCount();
            if (0 < pingCount) {
                PingConfig pingConfig = config.getPingConfig();
                KUID localhost = dht.getLocalhost().getContactId();
                long contactTimeout = config.getContactTimeoutInMillis();
                
                Contact[] contacts = routeTable.select(localhost, pingCount);
                for (Contact contact : contacts) {
                    if (contact.isTimeout(contactTimeout, TimeUnit.MILLISECONDS)) {
                        ArdverkFuture<PingEntity> future = dht.ping(
                                queueKey, contact, pingConfig);
                        futures.add(future);
                    }
                }
            }
            
            LookupConfig lookupConfig = config.getLookupConfig();
            long bucketTimeout = config.getBucketTimeoutInMillis();
            KUID[] bucketIds = routeTable.select(bucketTimeout, TimeUnit.MILLISECONDS);
            
            for (KUID bucketId : bucketIds) {
                ArdverkFuture<NodeEntity> future = dht.lookup(
                        queueKey, bucketId, lookupConfig);
                futures.add(future);
            }
        }
        
        return futures.toArray(new ArdverkFuture[0]);
    }
}
