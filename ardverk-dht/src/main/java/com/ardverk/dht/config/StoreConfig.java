package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface StoreConfig extends Config {
    
    public long getStoreTimeout(TimeUnit unit);
    
    public long getStoreTimeoutInMillis();
    
    public void setStoreTimeout(long timeout, TimeUnit unit);
}