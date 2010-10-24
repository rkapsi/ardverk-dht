package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.NopAsyncProcess;
import org.ardverk.concurrent.ValueReference;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.BootstrapEntity;
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
                
                private void handlePingEntity(PingEntity entity) {
                    Contact localhost = dht.getLocalhost();
                    AsyncFuture<NodeEntity> lookupFuture 
                        = lookupFutureRef.make(
                            dht.lookup(queueKey, localhost.getContactId(), 
                                config.getLookupConfig()));
                    
                    lookupFuture.addAsyncFutureListener(new AsyncFutureListener<NodeEntity>() {
                        @Override
                        public void operationComplete(AsyncFuture<NodeEntity> future) {
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
    }
}
