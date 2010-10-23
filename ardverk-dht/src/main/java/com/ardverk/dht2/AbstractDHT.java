package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;

abstract class AbstractDHT implements DHT {

    private final FutureManager futureManager = new FutureManager();
    
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
