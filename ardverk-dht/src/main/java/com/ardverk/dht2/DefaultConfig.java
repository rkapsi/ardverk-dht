package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultConfig implements Config {
    
    private volatile long timeoutInMillis;
    
    public DefaultConfig() {
    }
    
    public DefaultConfig(long timeout, TimeUnit unit) {
        setTimeout(timeout, unit);
    }
    
    @Override
    public void setTimeout(long timeout, TimeUnit unit) {
        this.timeoutInMillis = unit.toMillis(timeout);
    }
    
    @Override
    public long getTimeout(TimeUnit unit) {
        return unit.convert(timeoutInMillis, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
}