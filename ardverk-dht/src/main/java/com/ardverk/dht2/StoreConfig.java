package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public interface StoreConfig extends Config {
    
    public LookupConfig getLookupConfig();
    
    public long getCombinedTimeout(TimeUnit unit);
}