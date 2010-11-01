package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultLookupConfig extends DefaultConfig 
        implements LookupConfig {
    
    public DefaultLookupConfig() {
        super(1L, TimeUnit.MINUTES);
    }
}