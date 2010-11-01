package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultValueConfig extends DefaultConfig 
        implements ValueConfig {
    
    private volatile boolean exhaustive = false;

    public DefaultValueConfig() {
        super(1L, TimeUnit.MINUTES);
    }
    
    @Override
    public boolean isExhaustive() {
        return exhaustive;
    }

    @Override
    public void setExhaustive(boolean exhaustive) {
        this.exhaustive = exhaustive;
    }
}