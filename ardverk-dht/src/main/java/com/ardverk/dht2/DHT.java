package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.NodeStoreEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.routing.Contact;

public interface DHT {
    
    public ArdverkFuture<PingEntity> ping(String host, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(SocketAddress address, PingConfig config);
    
    public ArdverkFuture<PingEntity> ping(Contact dst, PingConfig config);
    
    public ArdverkFuture<NodeEntity> lookup(KUID key, LookupConfig config);
    
    public ArdverkFuture<ValueEntity> get(KUID key, ValueConfig config);
    
    public ArdverkFuture<NodeStoreEntity> put(KUID key, 
            Value value, StoreConfig config);
    
    public ArdverkFuture<StoreEntity> put(Contact[] dst, KUID key, 
            Value value, StoreConfig config);
    
    public ArdverkFuture<NodeStoreEntity> store(KUID key, 
            Value value, StoreConfig config);
    
    public ArdverkFuture<StoreEntity> store(Contact[] dst, KUID key, 
            Value value, StoreConfig config);
    
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process, Config config);
    
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process, long timeout, TimeUnit unit);
}
