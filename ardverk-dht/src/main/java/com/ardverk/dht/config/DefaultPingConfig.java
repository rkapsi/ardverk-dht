package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.utils.TimeUtils;

public class DefaultPingConfig extends DefaultConfig 
        implements PingConfig {
    
    private static final long DEFAULT_PING_TIMEOUT 
        = TimeUtils.convert(10L, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    
    public DefaultPingConfig() {
        super(DEFAULT_PING_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public DefaultPingConfig(long pingTimeout, TimeUnit unit) {
        super(pingTimeout, unit);
    }

    @Override
    public void setPingTimeout(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }

    @Override
    public long getPingTimeout(TimeUnit unit) {
        return getOperationTimeout(unit);
    }

    @Override
    public long getPingTimeoutInMillis() {
        return getOperationTimeoutInMillis();
    }
}