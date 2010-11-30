package com.ardverk.dht;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncProcess;

import com.ardverk.dht.concurrent.ArdverkFuture;
import com.ardverk.dht.config.Config;

abstract class AbstractDHT implements DHT, Closeable {

    private final FutureManager futureManager = new FutureManager();
    
    @Override
    public void close() {
        futureManager.close();
    }

    @Override
    public <V> ArdverkFuture<V> submit(AsyncProcess<V> process, Config config) {
        QueueKey queueKey = config.getQueueKey();
        long timeout = config.getOperationTimeoutInMillis();
        return submit(queueKey, process, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ArdverkFuture<V> submit(QueueKey queueKey,
            AsyncProcess<V> process, long timeout, TimeUnit unit) {
        return futureManager.submit(queueKey, process, timeout, unit);
    }
}
