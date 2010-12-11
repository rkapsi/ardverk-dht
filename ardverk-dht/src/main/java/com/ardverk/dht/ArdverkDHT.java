package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.codec.MessageCodec;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.GetConfig;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.SyncConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.RefreshEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.SyncEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.DefaultMessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.NodeResponseHandler;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.io.ValueResponseHandler;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.StoreForward;
import com.ardverk.dht.storage.ValueTuple;

public class ArdverkDHT extends AbstractDHT {
    
    private final BootstrapManager bootstrapManager;
    
    private final RouteTableManager routeTableManager;
    
    private final StoreManager storeManager;
    
    private final SyncManager syncManager;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    private final MessageDispatcher messageDispatcher;
    
    public ArdverkDHT(MessageCodec codec, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        
        this.routeTable = routeTable;
        this.database = database;
        
        StoreForward.Callback callback = new StoreForward.Callback() {
            @Override
            public ArdverkFuture<StoreEntity> store(Contact dst, 
                    ValueTuple valueTuple, StoreConfig config) {
                return storeManager.store(new Contact[] { dst }, valueTuple, config);
            }
        };
        
        StoreForward storeForward = new StoreForward(
                callback, routeTable, database);
        
        messageDispatcher = new DefaultMessageDispatcher(
                messageFactory, codec, storeForward, 
                routeTable, database);
        
        bootstrapManager = new BootstrapManager(this);
        routeTableManager = new RouteTableManager(this, routeTable);
        storeManager = new StoreManager(this, routeTable, 
                messageDispatcher);
        syncManager = new SyncManager(this, 
                storeManager, routeTable, database);
        
        routeTable.bind(new RouteTable.ContactPinger() {
            @Override
            public ArdverkFuture<PingEntity> ping(Contact contact,
                    PingConfig config) {
                return ArdverkDHT.this.ping(contact, config);
            }
        });
    }
    
    @Override
    public void close() {
        super.close();
        messageDispatcher.close();
    }
    
    @Override
    public Contact getLocalhost() {
        return routeTable.getLocalhost();
    }
    
    @Override
    public RouteTable getRouteTable() {
        return routeTable;
    }

    @Override
    public Database getDatabase() {
        return database;
    }
    
    @Override
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
    
    public BootstrapManager getBootstrapManager() {
        return bootstrapManager;
    }

    public RouteTableManager getRouteTableManager() {
        return routeTableManager;
    }

    public StoreManager getStoreManager() {
        return storeManager;
    }
    
    public SyncManager getSyncManager() {
        return syncManager;
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
    public ArdverkFuture<RefreshEntity> refresh(RefreshConfig config) {
        return routeTableManager.refresh(config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(Contact contact, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact, config);
        return submit(process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(
            InetAddress address, int port, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port, config);
        return submit(process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(
            SocketAddress dst, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst, config);
        return submit(process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(
            String address, int port, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port, config);
        return submit(process, config);
    }

    @Override
    public ArdverkFuture<NodeEntity> lookup(Contact[] contacts, 
            KUID lookupId, LookupConfig config) {
        
        AsyncProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, 
                    contacts, routeTable, lookupId, config);
        ArdverkFuture<NodeEntity> future = submit(process, config);
        future.setAttachment(process);
        return future;
    }

    @Override
    public ArdverkFuture<ValueEntity> get(Contact[] contacts, 
            KUID key, GetConfig config) {
        AsyncProcess<ValueEntity> process
            = new ValueResponseHandler(messageDispatcher, contacts, 
                    routeTable, key, config);
        return submit(process, config);
    }

    @Override
    public ArdverkFuture<StoreEntity> put(KUID key, byte[] value, PutConfig config) {
        return storeManager.put(key, value, config);
    }
    
    @Override
    public ArdverkFuture<StoreEntity> store(
            Contact[] dst, KUID key, byte[] value, StoreConfig config) {
        return storeManager.put(dst, key, value, config);
    }
    
    /**
     * 
     */
    public ArdverkFuture<SyncEntity> sync(SyncConfig config) {
        return syncManager.sync(config);
    }
}