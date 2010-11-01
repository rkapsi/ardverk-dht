package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

public abstract class AbstractConfig implements Config {

    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
}
