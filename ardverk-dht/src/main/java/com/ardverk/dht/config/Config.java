package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface Config {
    
    public void setTimeout(long timeout, TimeUnit unit);
    
    public long getTimeout(TimeUnit unit);
    
    public long getTimeoutInMillis();
}