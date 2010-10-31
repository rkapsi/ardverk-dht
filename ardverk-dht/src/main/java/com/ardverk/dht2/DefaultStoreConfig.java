package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {

    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }
    
    @Override
    public long getCombinedTimeout(TimeUnit unit) {
        return ConfigUtils.getTimeout(unit, this, lookupConfig);
    }
}