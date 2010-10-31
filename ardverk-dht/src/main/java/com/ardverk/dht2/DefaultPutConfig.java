package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultPutConfig extends AbstractConfig implements PutConfig {

    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
    
    @Override
    public void setLookupConfig(LookupConfig lookupConfig) {
        this.lookupConfig = lookupConfig;
    }
    
    @Override
    public StoreConfig getStoreConfig() {
        return storeConfig;
    }
    
    @Override
    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public void setTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeout(TimeUnit unit) {
        return ConfigUtils.getTimeout(unit, lookupConfig, storeConfig);
    }
}
