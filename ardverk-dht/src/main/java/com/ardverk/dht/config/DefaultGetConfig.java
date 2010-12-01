package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultGetConfig extends DefaultLookupConfig 
        implements GetConfig {
    
    private volatile int r = 1;
    
    public DefaultGetConfig() {
        super();
    }

    public DefaultGetConfig(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }
    
    @Override
    public int getR() {
        return r;
    }

    @Override
    public void setR(int r) {
        this.r = r;
    }
}