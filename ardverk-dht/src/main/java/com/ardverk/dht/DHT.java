package com.ardverk.dht;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.BootstrapConfig;
import com.ardverk.dht.config.Config;
import com.ardverk.dht.config.LookupConfig;
import com.ardverk.dht.config.PingConfig;
import com.ardverk.dht.config.PutConfig;
import com.ardverk.dht.config.RefreshConfig;
import com.ardverk.dht.config.StoreConfig;
import com.ardverk.dht.config.GetConfig;
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

public interface DHT {
    
    public Contact getLocalhost();
    
    public RouteTable getRouteTable();
    
    public Database getDatabase();
    
    public MessageDispatcher getMessageDispatcher();
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            String host, int port, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            InetAddress address, int port, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            SocketAddress address, BootstrapConfig config);
    
    public ArdverkFuture<BootstrapEntity> bootstrap(
            Contact contact, BootstrapConfig config);
    
    public ArdverkFuture<RefreshEntity> refresh(RefreshConfig config);
    
    public ArdverkFuture<PingEntity> ping(
            String host, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(
            InetAddress address, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(
            SocketAddress address, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(
            Contact dst, PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(
            KUID lookupId, LookupConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(
            Contact[] contacts, KUID lookupId, LookupConfig config);
    
    public ArdverkFuture<ValueEntity> get(
            KUID key, GetConfig config);
    
    public ArdverkFuture<ValueEntity> get(
            Contact[] contacts, KUID key, GetConfig config);
    
    public ArdverkFuture<StoreEntity> put(
            KUID key, byte[] value, PutConfig config);
    
    public ArdverkFuture<StoreEntity> store(
            Contact[] dst, KUID key, byte[] value, StoreConfig config);
    
    public <V> ArdverkFuture<V> submit(
            AsyncProcess<V> process, Config config);
    
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, long timeout, TimeUnit unit);
}
