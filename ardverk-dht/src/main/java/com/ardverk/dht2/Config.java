package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public interface Config {
    
    public void setTimeout(long timeout, TimeUnit unit);
    
    public long getTimeout(TimeUnit unit);
    
    public long getTimeoutInMillis();
}