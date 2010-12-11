package com.ardverk.dht.config;

public interface SyncConfig {

    public PingConfig getPingConfig();
    
    public void setPingConfig(PingConfig pingConfig);
    
    public StoreConfig getStoreConfig();
    
    public void setStoreConfig(StoreConfig storeConfig);
}
