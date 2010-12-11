package com.ardverk.dht.storage;

import com.ardverk.dht.QueueKey;
import com.ardverk.dht.config.DefaultStoreConfig;
import com.ardverk.dht.config.StoreConfig;

public class DefaultDatabaseConfig implements DatabaseConfig {

    private volatile StoreConfig storeConfig = new DefaultStoreConfig();
    
    private volatile boolean checkBucket = false;
    
    private volatile boolean storeForwad = true;
    
    // INIT
    {
        storeConfig.setQueueKey(QueueKey.BACKEND);
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
    public boolean isStoreForward() {
        return storeForwad;
    }

    @Override
    public void setStoreForward(boolean storeForwad) {
        this.storeForwad = storeForwad;
    }

    @Override
    public boolean isCheckBucket() {
        return checkBucket;
    }
    
    @Override
    public void setCheckBucket(boolean checkBucket) {
        this.checkBucket = checkBucket;
    }
}
