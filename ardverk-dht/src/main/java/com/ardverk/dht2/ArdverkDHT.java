package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.DefaultMessageDispatcher;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.io.NodeResponseHandler;
import com.ardverk.dht.io.PingResponseHandler;
import com.ardverk.dht.io.ValueResponseHandler;
import com.ardverk.dht.message.AbstractMessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultKey;
import com.ardverk.dht.storage.Value;

public class ArdverkDHT extends AbstractDHT {
    
    private final StoreManager storeManager;
    
    private final RouteTable routeTable;
    
    private final Database database;
    
    private final MessageDispatcher messageDispatcher;
    
    public ArdverkDHT(AbstractMessageCodec codec, MessageFactory messageFactory, 
            RouteTable routeTable, Database database) {
        
        this.routeTable = routeTable;
        this.database = database;
        
        messageDispatcher = new DefaultMessageDispatcher(
                messageFactory, codec, routeTable, database);
        
        storeManager = new StoreManager(this, messageDispatcher);
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
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            Contact contact, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, contact);
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey,
            InetAddress address, int port, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            SocketAddress dst, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, dst);
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            String address, int port, PingConfig config) {
        AsyncProcess<PingEntity> process 
            = new PingResponseHandler(messageDispatcher, address, port);
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<NodeEntity> lookup(QueueKey queueKey, KUID key,
            LookupConfig config) {
        AsyncProcess<NodeEntity> process 
            = new NodeResponseHandler(messageDispatcher, routeTable, key);
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<ValueEntity> get(QueueKey queueKey, KUID key,
            ValueConfig config) {
        AsyncProcess<ValueEntity> process
            = new ValueResponseHandler(messageDispatcher, routeTable, 
                    new DefaultKey(key));
        return submit(queueKey, process, config);
    }

    @Override
    public ArdverkFuture<StoreEntity> put(QueueKey queueKey, 
            KUID key, Value value, StoreConfig config) {
        return storeManager.put(queueKey, key, value, config);
    }
    
    @Override
    public ArdverkFuture<StoreEntity> put(QueueKey queueKey, 
            Contact[] dst, KUID key, Value value, StoreConfig config) {
        return storeManager.put(queueKey, dst, key, value, config);
    }
}