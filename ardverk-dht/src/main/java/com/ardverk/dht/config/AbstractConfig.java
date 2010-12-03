package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.QueueKey;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractConfig implements Config {

    private volatile double adaptiveTimeoutMultiplier = -1;
    
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
    public double getRoundTripTimeMultiplier() {
        return adaptiveTimeoutMultiplier;
    }

    @Override
    public void setRountTripTimeMultiplier(double adaptiveTimeoutMultiplier) {
        this.adaptiveTimeoutMultiplier = adaptiveTimeoutMultiplier;
    }
    
    @Override
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        double multiplier = getRoundTripTimeMultiplier();
        return dst.getAdaptiveTimeout(multiplier, defaultTimeout, unit);
    }
}
