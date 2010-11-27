package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import org.ardverk.utils.TimeUtils;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {
    
    private static final long DEFAULT_STORE_TIMEOUT
        = TimeUtils.convert(1L, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
    
    public DefaultStoreConfig() {
        super(DEFAULT_STORE_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public DefaultStoreConfig(long storeTimeout, TimeUnit unit) {
        super(storeTimeout, unit);
    }

    @Override
    public long getStoreTimeout(TimeUnit unit) {
        return getOperationTimeout(unit);
    }

    @Override
    public long getStoreTimeoutInMillis() {
        return getOperationTimeoutInMillis();
    }

    @Override
    public void setStoreTimeout(long timeout, TimeUnit unit) {
        setOperationTimeout(timeout, unit);
    }
}