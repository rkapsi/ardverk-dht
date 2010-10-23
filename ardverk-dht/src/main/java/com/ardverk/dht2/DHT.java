package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Value;

public interface DHT {
    
    public Contact getContact();
    
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
            KUID key, Value value, StoreConfig config);
    
    public ArdverkFuture<StoreEntity> put(QueueKey queueKey, 
            Contact[] dst, KUID key, Value value, StoreConfig config);
    
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, Config config);
    
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, long timeout, TimeUnit unit);
}
