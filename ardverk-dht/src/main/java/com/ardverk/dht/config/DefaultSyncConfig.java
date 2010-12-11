package com.ardverk.dht.config;

import com.ardverk.dht.QueueKey;

public class DefaultSyncConfig implements SyncConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    // INIT
    {
        pingConfig.setQueueKey(QueueKey.BACKEND);
        storeConfig.setQueueKey(QueueKey.BACKEND);
    }
    
    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }
    
    @Override
    public void setPingConfig(PingConfig pingConfig) {
        this.pingConfig = pingConfig;
    }
    
    @Override
    public StoreConfig getStoreConfig() {
        return storeConfig;
    }
    
    @Override
    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }
}
