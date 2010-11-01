package com.ardverk.dht.config;

public interface PutConfig extends Config {

    public LookupConfig getLookupConfig();
    
    public void setLookupConfig(LookupConfig lookupConfig);
    
    public StoreConfig getStoreConfig();
    
    public void setStoreConfig(StoreConfig storeConfig);
}
