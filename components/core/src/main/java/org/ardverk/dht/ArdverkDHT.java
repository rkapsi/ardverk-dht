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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.config.BootstrapConfig;
import org.ardverk.dht.config.GetConfig;
import org.ardverk.dht.config.LookupConfig;
import org.ardverk.dht.config.PingConfig;
import org.ardverk.dht.config.PutConfig;
import org.ardverk.dht.config.QuickenConfig;
import org.ardverk.dht.entity.BootstrapEntity;
import org.ardverk.dht.entity.NodeEntity;
import org.ardverk.dht.entity.PingEntity;
import org.ardverk.dht.entity.PutEntity;
import org.ardverk.dht.entity.QuickenEntity;
import org.ardverk.dht.entity.ValueEntity;
import org.ardverk.dht.io.MessageDispatcher;
import org.ardverk.dht.io.transport.Transport;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.routing.RouteTable;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Datastore;
import org.ardverk.lang.BindableUtils;


/**
 * The Ardverk Distributed Hash Table (DHT).
 */
@Singleton
public class ArdverkDHT extends AbstractDHT {
    
    private final BootstrapManager bootstrapManager;
    
    private final QuickenManager quickenManager;
    
    private final StoreManager storeManager;
    
    private final LookupManager lookupManager;
    
    private final PingManager pingManager;
    
    private final MessageDispatcher messageDispatcher;
    
    @Inject
    public ArdverkDHT(RouteTable routeTable, 
            Datastore datastore,
            FutureManager futureManager, 
            PingManager pingManager, 
            BootstrapManager bootstrapManager,
            QuickenManager quickenManager,
            StoreManager storeManager,
            LookupManager lookupManager,
            MessageDispatcher messageDispatcher) {
        super(routeTable, datastore, futureManager);
        
        this.messageDispatcher = messageDispatcher;
        
        this.pingManager = pingManager;
        this.bootstrapManager = bootstrapManager;
        this.quickenManager = quickenManager;
        this.storeManager = storeManager;
        this.lookupManager = lookupManager;
        
        BindableUtils.bind(routeTable, new RouteTable.ContactPinger() {
            @Override
            public DHTFuture<PingEntity> ping(Contact contact,
                    PingConfig config) {
                return ArdverkDHT.this.ping(contact, config);
            }
        });
        
        BindableUtils.bind(datastore, this);
    }
    
    @Override
    public void close() {
        super.close();
        messageDispatcher.close();
        
        BindableUtils.unbind(datastore);
        BindableUtils.unbind(routeTable);
    }
    
    /**
     * Returns the {@link MessageDispatcher}.
     */
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
    
    /**
     * Returns the {@link BootstrapManager}.
     */
    public BootstrapManager getBootstrapManager() {
        return bootstrapManager;
    }

    /**
     * Returns the {@link QuickenManager}.
     */
    public QuickenManager getQuickenManager() {
        return quickenManager;
    }

    /**
     * Returns the {@link StoreManager}.
     */
    public StoreManager getStoreManager() {
        return storeManager;
    }
    
    /**
     * Returns the {@link LookupManager}.
     */
    public LookupManager getLookupManager() {
        return lookupManager;
    }
    
    /**
     * Returns the {@link PingManager}.
     */
    public PingManager getPingManager() {
        return pingManager;
    }
    
    @Override
    public void bind(Transport transport) throws IOException {
        messageDispatcher.bind(transport);
    }

    @Override
    public void unbind() {
        messageDispatcher.unbind();
    }

    @Override
    public boolean isBound() {
        return messageDispatcher.isBound();
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(
            String host, int port, BootstrapConfig... config) {
        return bootstrapManager.bootstrap(host, port, config);
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port, BootstrapConfig... config) {
        return bootstrapManager.bootstrap(address, port, config);
    }

    @Override
    public DHTFuture<BootstrapEntity> bootstrap(
            SocketAddress address, BootstrapConfig... config) {
        return bootstrapManager.bootstrap(address, config);
    }
    
    @Override
    public DHTFuture<BootstrapEntity> bootstrap(
            Contact contact, BootstrapConfig... config) {
        return bootstrapManager.bootstrap(contact, config);
    }
    
    @Override
    public DHTFuture<PingEntity> ping(Contact contact, PingConfig... config) {
        return pingManager.ping(contact, config);
    }

    @Override
    public DHTFuture<PingEntity> ping(SocketAddress dst, PingConfig... config) {
        return pingManager.ping(dst, config);
    }

    @Override
    public DHTFuture<NodeEntity> lookup(KUID lookupId, LookupConfig... config) {
        return lookupManager.lookup(lookupId, config);
    }
    
    @Override
    public DHTFuture<ValueEntity> get(Key key, GetConfig... config) {
        return lookupManager.get(key, config);
    }

    @Override
    public DHTFuture<PutEntity> put(Key key, 
            Value value, PutConfig... config) {
        return storeManager.put(key, value, config);
    }

    @Override
    public DHTFuture<QuickenEntity> quicken(QuickenConfig... config) {
        return quickenManager.quicken(config);
    }
}