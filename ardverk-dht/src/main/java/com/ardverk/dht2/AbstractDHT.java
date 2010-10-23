package com.ardverk.dht2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.KUID;
import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.storage.Value;

public abstract class AbstractDHT implements DHT {

    private final FutureManager futureManager = new FutureManager();
    
    private final StoreManager storeManager;
    
    public AbstractDHT() {
        MessageDispatcher messageDispatcher = null;
        storeManager = new StoreManager(this, messageDispatcher);
    }
    
    @Override
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey,
            String hostname, int port, PingConfig config) {
        return ping(queueKey, new InetSocketAddress(hostname, port), config);
    }

    @Override
    public ArdverkFuture<PingEntity> ping(QueueKey queueKey, 
            InetAddress address, int port, PingConfig config) {
        return ping(queueKey, new InetSocketAddress(address, port), config);
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
    
    @Override
    public <V> ArdverkFuture<V> submit(QueueKey queueKey, 
            AsyncProcess<V> process, Config config) {
        long timeout = config.getTimeoutInMillis();
        return submit(queueKey, process, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ArdverkFuture<V> submit(QueueKey queueKey,
            AsyncProcess<V> process, long timeout, TimeUnit unit) {
        return futureManager.submit(queueKey, process, timeout, unit);
    }
}