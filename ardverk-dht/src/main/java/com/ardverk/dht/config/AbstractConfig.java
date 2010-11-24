package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.routing.Contact;
import com.ardverk.dht2.Constants;

public abstract class AbstractConfig implements Config {

    @Override
    public long getTimeoutInMillis() {
        return getTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        return dst.getAdaptiveTimeout(Constants.MULTIPLIER, 
                defaultTimeout, unit);
    }
}
