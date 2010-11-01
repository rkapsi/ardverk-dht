package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultStoreConfig extends DefaultConfig 
        implements StoreConfig {
    
    public DefaultStoreConfig() {
        super(1L, TimeUnit.MINUTES);
    }
}