package com.ardverk.dht;

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
import com.ardverk.dht.config.BootstrapConfig;
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
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            String host, int port, BootstrapConfig config) {
        return bootstrap(new InetSocketAddress(host, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port, BootstrapConfig config) {
        return bootstrap(new InetSocketAddress(address, port), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            Contact contact, BootstrapConfig config) {
        return bootstrap(contact.getContactAddress(), config);
    }
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            SocketAddress address, BootstrapConfig config) {
        
        Object lock = new Object();
        ArdverkFuture<PingEntity> pingFuture = dht.ping(
                address, config.getPingConfig());
        
        synchronized (lock) {
            return bootstrap(lock, pingFuture, config);
        }
    }
    
    private ArdverkFuture<BootstrapEntity> bootstrap(final Object lock, 
            final ArdverkFuture<PingEntity> pingFuture, 
            final BootstrapConfig config) {
        
        // Make sure we're holding the lock!
        assert (Thread.holdsLock(lock));
        
        AsyncProcess<BootstrapEntity> process = NopAsyncProcess.create();
        final ArdverkFuture<BootstrapEntity> userFuture 
            = dht.submit(process, config);
        
        final ValueReference<ArdverkFuture<NodeEntity>> lookupFutureRef
            = new ValueReference<ArdverkFuture<NodeEntity>>();
        
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
                Contact[] contacts = new Contact[] { pingEntity.getContact() };
                
                Contact localhost = dht.getLocalhost();
                KUID localhostId = localhost.getId();
                AsyncFuture<NodeEntity> lookupFuture 
                    = lookupFutureRef.make(
                        dht.lookup(contacts, localhostId, 
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
        
        userFuture.setAttachment(new Attachment(pingFuture, lookupFutureRef));
        return userFuture;
    }
    
    public static class Attachment {
        
        private final ArdverkFuture<PingEntity> pingFuture;
        
        private final ValueReference<ArdverkFuture<NodeEntity>> lookupFutureRef;
        
        private Attachment(ArdverkFuture<PingEntity> pingFuture, 
                ValueReference<ArdverkFuture<NodeEntity>> lookupFutureRef) {
            this.pingFuture = pingFuture;
            this.lookupFutureRef = lookupFutureRef;
        }
        
        public ArdverkFuture<PingEntity> getPingFuture() {
            return pingFuture;
        }
        
        public ArdverkFuture<NodeEntity> getLookupFuture() {
            return lookupFutureRef.get();
        }
    }
}
