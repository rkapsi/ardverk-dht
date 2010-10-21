package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.FutureManager;
import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.NodeStoreEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractDHT implements DHT {

    private final FutureManager futureManager = new FutureManager();
    
    private final StoreManager storeManager = new StoreManager(this);
    
    @Override
    public ArdverkFuture<PingEntity> ping(String hostname, int port, 
            PingConfig config) {
        return ping(new InetSocketAddress(hostname, port), config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(InetAddress address, int port,
            PingConfig config) {
        return ping(new InetSocketAddress(address, port), config);
    }

    @Override
    public ArdverkFuture<NodeStoreEntity> put(KUID key, 
            Value value, StoreConfig config) {
        return storeManager.put(key, value, config);
    }
    
    @Override
    public ArdverkFuture<StoreEntity> put(Contact[] dst, KUID key, 
            Value value, StoreConfig config) {
        return storeManager.put(dst, key, value, config);
    }

    @Override
    public ArdverkFuture<StoreEntity> store(Contact[] dst, KUID key, 
            Value value, StoreConfig config) {
        return storeManager.store(dst, key, value, config);
    }
    
    @Override
    public ArdverkFuture<NodeStoreEntity> store(KUID key, 
            Value value, StoreConfig config) {
        return storeManager.store(key, value, config);
    }
    
    @Override
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process, Config config) {
        long timeout = config.getTimeoutInMillis();
        return submit(process, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process,
            long timeout, TimeUnit unit) {
        return futureManager.submit(process, timeout, unit);
    }
}