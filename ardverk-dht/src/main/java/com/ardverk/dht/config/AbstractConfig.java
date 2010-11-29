package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.Constants;
import com.ardverk.dht.QueueKey;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractConfig implements Config {

    private volatile QueueKey queueKey = QueueKey.DEFAULT;
    
    @Override
    public QueueKey getQueueKey() {
        return queueKey;
    }

    @Override
    public void setQueueKey(QueueKey queueKey) {
        this.queueKey = queueKey;
    }

    @Override
    public final long getOperationTimeoutInMillis() {
        return getOperationTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        return dst.getAdaptiveTimeout(Constants.MULTIPLIER, 
                defaultTimeout, unit);
    }
}
