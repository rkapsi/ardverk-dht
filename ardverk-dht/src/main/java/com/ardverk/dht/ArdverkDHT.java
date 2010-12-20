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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

import com.ardverk.dht.codec.DefaultMessageCodec;
import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.QuickenConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.PutEntity;
import com.ardverk.dht.entity.QuickenEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.DefaultMessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.transport.DatagramTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.Localhost;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;
import com.ardverk.dht.storage.StoreForward;
import com.ardverk.dht.storage.ValueTuple;

/**
 * The Ardverk Distributed Hash Table (DHT).
 */
public class ArdverkDHT extends AbstractDHT {
    
    private final BootstrapManager bootstrapManager;
    
    private final QuickenManager quickenManager;
    
    private final StoreManager storeManager;
    
    private final SyncManager syncManager;
    
    private final LookupManager lookupManager;
    
    private final PingManager pingManager;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    private final MessageDispatcher messageDispatcher;
    
    public ArdverkDHT(int keySize) {
        this(new Localhost(keySize));
    }
    
    public ArdverkDHT(Localhost localhost) {
        this(new DefaultRouteTable(localhost));
    }
    
    public ArdverkDHT(RouteTable routeTable) {
        this(routeTable, new DefaultDatabase());
    }
    
    public ArdverkDHT(RouteTable routeTable, Database database) {
        this(new DefaultMessageCodec(), routeTable, database);
    }
    
    public ArdverkDHT(MessageCodec codec, 
            RouteTable routeTable, Database database) {
        this(codec, new DefaultMessageFactory(
                routeTable.getLocalhost()), routeTable, database);
    }
    
    public ArdverkDHT(MessageCodec codec, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        
        this.routeTable = routeTable;
        this.database = database;
        
        StoreForward storeForward 
            = new StoreForward(routeTable, database);
        
        messageDispatcher = new DefaultMessageDispatcher(
                messageFactory, codec, storeForward, 
                routeTable, database);
        
        pingManager = new PingManager(this, messageDispatcher);
        bootstrapManager = new BootstrapManager(this);
        quickenManager = new QuickenManager(this, routeTable);
        storeManager = new StoreManager(this, routeTable, 
                messageDispatcher);
        syncManager = new SyncManager(pingManager, 
                storeManager, routeTable, database);
        lookupManager = new LookupManager(this, 
                messageDispatcher, routeTable);
        
        routeTable.bind(new RouteTable.ContactPinger() {
            @Override
            public ArdverkFuture<PingEntity> ping(Contact contact,
                    PingConfig config) {
                return ArdverkDHT.this.ping(contact, config);
            }
        });
        
        storeForward.bind(new StoreForward.Callback() {
            @Override
            public ArdverkFuture<StoreEntity> store(Contact dst, 
                    ValueTuple valueTuple, StoreConfig config) {
                return storeManager.store(new Contact[] { dst }, valueTuple, config);
            }
        });
    }
    
    @Override
    public void close() {
        super.close();
        messageDispatcher.close();
    }
    
    @Override
    public RouteTable getRouteTable() {
        return routeTable;
    }

    @Override
    public Database getDatabase() {
        return database;
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
     * Returns the {@link SyncManager}.
     */
    public SyncManager getSyncManager() {
        return syncManager;
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
    public void bind(SocketAddress address) throws IOException {
        bind(new DatagramTransport(address));
    }

    @Override
    public void bind(Transport transport) throws IOException {
        getLocalhost().bind(transport);
        messageDispatcher.bind(transport);
    }

    @Override
    public void unbind() {
        messageDispatcher.unbind();
        getLocalhost().unbind();
    }

    @Override
    public boolean isBound() {
        return messageDispatcher.isBound();
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(
            String host, int port, BootstrapConfig config) {
        return bootstrapManager.bootstrap(host, port, config);
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port, BootstrapConfig config) {
        return bootstrapManager.bootstrap(address, port, config);
    }

    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(
            SocketAddress address, BootstrapConfig config) {
        return bootstrapManager.bootstrap(address, config);
    }
    
    @Override
    public ArdverkFuture<BootstrapEntity> bootstrap(
            Contact contact, BootstrapConfig config) {
        return bootstrapManager.bootstrap(contact, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(Contact contact, PingConfig config) {
        return pingManager.ping(contact, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(SocketAddress dst, PingConfig config) {
        return pingManager.ping(dst, config);
    }

    @Override
    public ArdverkFuture<NodeEntity> lookup(KUID lookupId, LookupConfig config) {
        return lookupManager.lookup(lookupId, config);
    }
    
    @Override
    public ArdverkFuture<ValueEntity> get(KUID key, GetConfig config) {
        return lookupManager.get(key, config);
    }

    @Override
    public ArdverkFuture<PutEntity> put(KUID key, byte[] value, PutConfig config) {
        return storeManager.put(key, value, config);
    }
    
    @Override
    public ArdverkFuture<PutEntity> remove(KUID key, PutConfig config) {
        return storeManager.remove(key, config);
    }
    
    @Override
    public ArdverkFuture<QuickenEntity> quicken(QuickenConfig config) {
        return quickenManager.quicken(config);
    }
    
    @Override
    public ArdverkFuture<SyncEntity> sync(SyncConfig config) {
        return syncManager.sync(config);
    }
}