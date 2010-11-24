package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public class DefaultValueConfig extends DefaultLookupConfig 
        implements ValueConfig {

    public DefaultValueConfig() {
        super();
    }

    public DefaultValueConfig(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }
}