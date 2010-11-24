package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {
    
    private volatile long fooTimeoutInMillis = 10L * 1000L;
    
    public DefaultStoreConfig() {
        super(1L, TimeUnit.MINUTES);
    }
    
    @Override
    public long getFooTimeout(TimeUnit unit) {
        return unit.convert(fooTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getFooTimeoutInMillis() {
        return getFooTimeout(TimeUnit.MILLISECONDS);
    }

    @Override
    public void setFooTimeout(long timeout, TimeUnit unit) {
        this.fooTimeoutInMillis = unit.toMillis(timeout);
    }
}