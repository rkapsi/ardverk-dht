package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public interface PingConfig extends Config {
    
    public void setPingTimeout(long timeout, TimeUnit unit);
    
    public long getPingTimeout(TimeUnit unit);
    
    public long getPingTimeoutInMillis();
}