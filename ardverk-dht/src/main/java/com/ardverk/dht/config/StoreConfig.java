package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface StoreConfig extends Config {
    
    public long getFooTimeout(TimeUnit unit);
    
    public long getFooTimeoutInMillis();
    
    public void setFooTimeout(long timeout, TimeUnit unit);
}