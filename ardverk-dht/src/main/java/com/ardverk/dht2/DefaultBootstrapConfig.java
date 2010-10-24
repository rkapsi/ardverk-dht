package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultBootstrapConfig extends DefaultConfig 
        implements BootstrapConfig {

    public DefaultBootstrapConfig() {
        super();
    }

    public DefaultBootstrapConfig(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }
}
