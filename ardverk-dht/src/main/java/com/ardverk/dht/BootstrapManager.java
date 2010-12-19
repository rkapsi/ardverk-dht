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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncFutureListener;
import org.ardverk.concurrent.FutureUtils;
import org.ardverk.concurrent.ValueReference;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.concurrent.ArdverkProcess;
import com.ardverk.dht.concurrent.NopArdverkProcess;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.DefaultBootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.routing.IContact;

public class BootstrapManager {

    private final ArdverkDHT dht;
    
    BootstrapManager(ArdverkDHT dht) {
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
            IContact contact, BootstrapConfig config) {
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
        
        ArdverkProcess<BootstrapEntity> process = NopArdverkProcess.create();
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
                IContact[] contacts = new IContact[] { pingEntity.getContact() };
                LookupManager lookupManager = dht.getLookupManager();
                
                IContact localhost = dht.getLocalhost();
                KUID localhostId = localhost.getId();
                AsyncFuture<NodeEntity> lookupFuture 
                    = lookupFutureRef.make(
                            lookupManager.lookup(contacts, 
                                localhostId, config.getLookupConfig()));
                
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