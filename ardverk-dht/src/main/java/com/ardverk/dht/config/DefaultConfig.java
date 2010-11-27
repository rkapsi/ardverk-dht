package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultConfig extends AbstractConfig {
    
    private volatile long operationTimeoutInMillis;
    
    public DefaultConfig() {
    }
    
    public DefaultConfig(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }
    
    @Override
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        this.operationTimeoutInMillis = unit.toMillis(timeout);
    }
    
    @Override
    public long getOperationTimeout(TimeUnit unit) {
        return unit.convert(operationTimeoutInMillis, TimeUnit.MILLISECONDS);
    }
}