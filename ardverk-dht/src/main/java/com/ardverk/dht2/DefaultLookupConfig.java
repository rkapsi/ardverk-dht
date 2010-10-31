package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultLookupConfig extends DefaultConfig 
        implements LookupConfig {
    
    public DefaultLookupConfig() {
        super(1L, TimeUnit.MINUTES);
    }
}