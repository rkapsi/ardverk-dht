package com.ardverk.dht.config;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.Constants;
import com.ardverk.dht.routing.Contact;

public abstract class AbstractConfig implements Config {

    @Override
    public final long getOperationTimeoutInMillis() {
        return getOperationTimeout(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public long getAdaptiveTimeout(Contact dst, 
            long defaultTimeout, TimeUnit unit) {
        return dst.getAdaptiveTimeout(Constants.MULTIPLIER, 
                defaultTimeout, unit);
    }
}
