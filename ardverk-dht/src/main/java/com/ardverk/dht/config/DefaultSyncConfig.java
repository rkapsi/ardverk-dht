package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.QueueKey;

public class DefaultSyncConfig extends AbstractConfig implements SyncConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    @Override
    public void setQueueKey(QueueKey queueKey) {
        super.setQueueKey(queueKey);
        pingConfig.setQueueKey(queueKey);
        storeConfig.setQueueKey(queueKey);
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

    @Override
    public void setOperationTimeout(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOperationTimeout(TimeUnit unit) {
        return ConfigUtils.getOperationTimeout(new Config[] { pingConfig, storeConfig }, unit);
    }
}
