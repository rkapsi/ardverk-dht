package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultValueConfig extends DefaultLookupConfig 
        implements ValueConfig {

    private static final int R = 1;
    
    private volatile int r = R;
    
    public DefaultValueConfig() {
        super();
    }

    public DefaultValueConfig(long timeout, TimeUnit unit) {
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