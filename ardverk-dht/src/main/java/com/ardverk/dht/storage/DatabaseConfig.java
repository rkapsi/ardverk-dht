package com.ardverk.dht.storage;

import com.ardverk.dht.config.StoreConfig;

public interface DatabaseConfig {

    public boolean isStoreForward();
    
    public void setStoreForward(boolean storeForward);
    
    public boolean isCheckBucket();
    
    public void setCheckBucket(boolean checkBucket);
    
    public StoreConfig getStoreConfig();

    public void setStoreConfig(StoreConfig storeConfig);
}
