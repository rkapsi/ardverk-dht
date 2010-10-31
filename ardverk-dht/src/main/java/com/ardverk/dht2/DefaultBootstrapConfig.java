package com.ardverk.dht2;

import java.util.concurrent.TimeUnit;

public class DefaultBootstrapConfig extends DefaultConfig 
        implements BootstrapConfig {

    private volatile PingConfig pingConfig = new DefaultPingConfig();
    
    private volatile LookupConfig lookupConfig = new DefaultLookupConfig();
    
    public DefaultBootstrapConfig() {
        super();
    }

    public DefaultBootstrapConfig(long timeout, TimeUnit unit) {
        super(timeout, unit);
    }

    @Override
    public PingConfig getPingConfig() {
        return pingConfig;
    }

    @Override
    public LookupConfig getLookupConfig() {
        return lookupConfig;
    }
}
