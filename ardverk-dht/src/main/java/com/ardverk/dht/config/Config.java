package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;

public interface Config {
    
    public void setOperationTimeout(long timeout, TimeUnit unit);
    
    public long getOperationTimeout(TimeUnit unit);
    
    public long getOperationTimeoutInMillis();
    
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit);
}