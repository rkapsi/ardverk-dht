package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {

    private final LookupConfig lookupConfig;
    
    public DefaultStoreConfig() {
        this(new DefaultLookupConfig());
    }
    
    public DefaultStoreConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }
    
    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public long getCombinedTimeout(TimeUnit unit) {
        return getTimeout(unit) + lookupConfig.getTimeout(unit);
    }
}