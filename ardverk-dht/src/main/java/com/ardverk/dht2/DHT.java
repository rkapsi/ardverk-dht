package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.Config;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.ValueConfig;
import com.ardverk.dht.entity.BootstrapEntity;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.RefreshEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.Value;

public interface DHT {
    
    public Contact getLocalhost();
    
    public RouteTable getRouteTable();
    
    public Database getDatabase();
    
    public MessageDispatcher getMessageDispatcher();
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            String host, int port, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            InetAddress address, int port, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            SocketAddress address, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(QueueKey queueKey, 
            Contact contact, BootstrapConfig config);
    
    public ArdverkFuture<RefreshEntity> refresh(QueueKey queueKey, RefreshConfig config);
    
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            String host, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            InetAddress address, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            SocketAddress address, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            Contact dst, PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(QueueKey queueKey, 
            KUID key, LookupConfig config);
    
    public ArdverkFuture<ValueEntity> get(QueueKey queueKey,
            KUID key, ValueConfig config);
    
    public ArdverkFuture<StoreEntity> put(QueueKey queueKey,
            KUID key, Value value, PutConfig config);
    
    public ArdverkFuture<StoreEntity> store(QueueKey queueKey, 
            Contact[] dst, KUID key, Value value, StoreConfig config);
    
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, Config config);
    
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, long timeout, TimeUnit unit);
}
