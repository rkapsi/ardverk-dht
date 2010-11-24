package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;

public interface Config {
    
    public void setTimeout(long timeout, TimeUnit unit);
    
    public long getTimeout(TimeUnit unit);
    
    public long getTimeoutInMillis();
    
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit);
}