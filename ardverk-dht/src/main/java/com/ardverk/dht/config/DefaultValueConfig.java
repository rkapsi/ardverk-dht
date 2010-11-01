package com.ardverk.dht.config;

public class DefaultValueConfig extends DefaultLookupConfig 
        implements ValueConfig {
    
    private volatile boolean exhaustive = false;

    @Override
    public boolean isExhaustive() {
        return exhaustive;
    }

    @Override
    public void setExhaustive(boolean exhaustive) {
        this.exhaustive = exhaustive;
    }
}